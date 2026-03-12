<template>
  <div class="upload-page">
    <div class="page-header">
      <h2>📄 文档管理</h2>
      <p>上传小说文本文件，系统将自动分块、嵌入并索引到向量数据库</p>
    </div>
    <div class="page-body">
      <div class="upload-scroll">

        <!-- Upload Zone -->
        <div
          class="upload-zone"
          :class="{ dragging }"
          @dragover.prevent="dragging = true"
          @dragleave="dragging = false"
          @drop.prevent="handleDrop"
          @click="$refs.fileInput.click()"
          id="upload-zone"
        >
          <input type="file" ref="fileInput" accept=".txt,.md,.text" style="display:none" @change="handleFileSelect" />
          <span class="upload-icon">📤</span>
          <h3>拖拽文件到此处或点击上传</h3>
          <p>支持 .txt / .md 文件，自动检测 UTF-8 / GBK 编码</p>
        </div>

        <!-- Upload Progress -->
        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="card" style="margin-top: 16px;">
          <div class="card-title">⬆️ 上传中...</div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
          </div>
          <div class="progress-label">{{ uploadProgress }}%</div>
        </div>

        <!-- Document List -->
        <div class="doc-list" v-if="documents.length > 0">
          <div class="card-title" style="margin-top: 24px; padding: 0;">📚 已上传文档（{{ documents.length }}）</div>
          <div v-for="doc in documents" :key="doc.id" class="doc-item" :class="{'doc-item-active': isIndexing(doc)}">
            <div class="doc-info">
              <div class="doc-icon">{{ docIcon(doc) }}</div>
              <div class="doc-detail">
                <div class="doc-name">{{ doc.fileName }}</div>
                <div class="doc-meta">
                  {{ formatSize(doc.fileSizeBytes || doc.fileSize) }}
                  <span v-if="doc.sourceEncoding"> · {{ doc.sourceEncoding }}</span>
                </div>

                <!-- Pipeline Stage Visualization -->
                <div v-if="isIndexing(doc) || isIndexed(doc)" class="pipeline-stages">
                  <div class="stage" :class="stageClass(doc, 'CHUNKING')">
                    <span class="stage-icon">{{ stageIcon(doc, 'CHUNKING') }}</span>
                    <span class="stage-label">分块</span>
                  </div>
                  <div class="stage-arrow">→</div>
                  <div class="stage" :class="stageClass(doc, 'EMBEDDING')">
                    <span class="stage-icon">{{ stageIcon(doc, 'EMBEDDING') }}</span>
                    <span class="stage-label">嵌入</span>
                  </div>
                  <div class="stage-arrow">→</div>
                  <div class="stage" :class="stageClass(doc, 'PERSISTING')">
                    <span class="stage-icon">{{ stageIcon(doc, 'PERSISTING') }}</span>
                    <span class="stage-label">持久化</span>
                  </div>
                  <div class="stage-arrow">→</div>
                  <div class="stage" :class="stageClass(doc, 'DONE')">
                    <span class="stage-icon">{{ stageIcon(doc, 'DONE') }}</span>
                    <span class="stage-label">完成</span>
                  </div>
                </div>

                <!-- Chunk Progress Bar -->
                <div v-if="isIndexing(doc) && doc.jobInfo" class="chunk-progress">
                  <div class="progress-bar" style="height: 6px;">
                    <div class="progress-fill" :style="{ width: chunkPercent(doc) + '%' }"></div>
                  </div>
                  <div class="chunk-stats">
                    <span>{{ doc.jobInfo.processedChunks || 0 }} / {{ doc.jobInfo.totalChunks || '?' }} 分块</span>
                    <span v-if="doc.jobInfo.currentStage">阶段: {{ stageLabels[doc.jobInfo.currentStage] || doc.jobInfo.currentStage }}</span>
                  </div>
                </div>

                <!-- Indexed Summary -->
                <div v-if="isIndexed(doc) && doc.jobInfo" class="indexed-summary">
                  ✅ {{ doc.jobInfo.totalChunks || doc.jobInfo.processedChunks }} 个分块已索引
                </div>

                <!-- Error Info -->
                <div v-if="isError(doc)" class="error-info">
                  ❌ {{ doc.errorMessage || '索引失败' }}
                </div>
              </div>
            </div>
            <div class="doc-actions">
              <span class="doc-status" :class="'status-' + (doc.documentStatus || doc.status || '').toLowerCase()">
                {{ statusLabels[doc.documentStatus || doc.status] || doc.documentStatus || doc.status }}
              </span>
              <button
                v-if="canIndex(doc)"
                class="btn btn-primary btn-sm"
                @click="startIndex(doc.id)"
                :id="'index-btn-' + doc.id"
              >
                🚀 开始索引
              </button>
              <button
                v-if="isError(doc)"
                class="btn btn-sm"
                style="background: var(--accent-secondary); color: #fff;"
                @click="startIndex(doc.id)"
              >
                🔄 重试
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="documents.length === 0 && !uploadProgress" class="empty-state" style="margin-top: 40px;">
          <div class="empty-icon">📚</div>
          <h3>暂无文档</h3>
          <p>上传一部小说开始分析人物关系</p>
        </div>

      </div>
    </div>

    <!-- Toast -->
    <div v-if="toast.show" :class="'toast toast-' + toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { uploadDocument, listDocuments, indexDocument, getLatestJob } from '../api'

const documents = ref([])
const dragging = ref(false)
const uploadProgress = ref(0)
const toast = ref({ show: false, type: 'success', message: '' })
const fileInput = ref(null)

let pollTimers = {}

const statusLabels = {
  UPLOADED: '待索引',
  INDEXING: '索引中...',
  INDEXED: '已索引 ✓',
  ERROR: '错误 ✗'
}

const stageLabels = {
  CHUNKING: '分块中',
  EMBEDDING: '嵌入中',
  PERSISTING: '持久化中',
  DONE: '完成'
}

// Pipeline stage ordering for comparison
const stageOrder = { CHUNKING: 1, EMBEDDING: 2, PERSISTING: 3, DONE: 4 }

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function isIndexing(doc) {
  return (doc.documentStatus || doc.status) === 'INDEXING'
}

function isIndexed(doc) {
  return (doc.documentStatus || doc.status) === 'INDEXED'
}

function isError(doc) {
  return (doc.documentStatus || doc.status) === 'ERROR'
}

function canIndex(doc) {
  const s = doc.documentStatus || doc.status
  return s === 'UPLOADED'
}

function docIcon(doc) {
  const s = doc.documentStatus || doc.status
  if (s === 'INDEXED') return '📗'
  if (s === 'INDEXING') return '⏳'
  if (s === 'ERROR') return '📕'
  return '📄'
}

function chunkPercent(doc) {
  if (!doc.jobInfo || !doc.jobInfo.totalChunks) return 0
  return Math.round((doc.jobInfo.processedChunks / doc.jobInfo.totalChunks) * 100)
}

function stageClass(doc, stage) {
  const currentStage = doc.jobInfo?.currentStage || (isIndexed(doc) ? 'DONE' : '')
  const currentOrder = stageOrder[currentStage] || 0
  const targetOrder = stageOrder[stage] || 0
  if (targetOrder < currentOrder) return 'stage-done'
  if (targetOrder === currentOrder) return isIndexed(doc) ? 'stage-done' : 'stage-active'
  return 'stage-pending'
}

function stageIcon(doc, stage) {
  const cls = stageClass(doc, stage)
  if (cls === 'stage-done') return '✅'
  if (cls === 'stage-active') return '⏳'
  return '⬜'
}

function showToast(type, message) {
  toast.value = { show: true, type, message }
  setTimeout(() => { toast.value.show = false }, 4000)
}

async function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) await upload(file)
  e.target.value = '' // Allow re-upload of same file
}

function handleDrop(e) {
  dragging.value = false
  const file = e.dataTransfer.files[0]
  if (file) upload(file)
}

async function upload(file) {
  try {
    uploadProgress.value = 1
    const res = await uploadDocument(file, (e) => {
      uploadProgress.value = Math.round((e.loaded / e.total) * 100)
    })
    const doc = res.data?.data || res.data
    documents.value.unshift(doc)
    uploadProgress.value = 0
    showToast('success', `📄 ${doc.fileName} 上传成功！(${formatSize(doc.fileSizeBytes)}, ${doc.sourceEncoding})`)
  } catch (err) {
    uploadProgress.value = 0
    showToast('error', '上传失败: ' + (err.response?.data?.message || err.message))
  }
}

async function startIndex(docId) {
  try {
    await indexDocument(docId)
    const idx = documents.value.findIndex(d => d.id === docId)
    if (idx !== -1) {
      documents.value[idx].documentStatus = 'INDEXING'
      documents.value[idx].jobInfo = { currentStage: 'CHUNKING', processedChunks: 0 }
    }
    showToast('success', '🚀 索引流水线已启动')
    startPolling(docId)
  } catch (err) {
    showToast('error', '索引启动失败: ' + (err.response?.data?.message || err.message))
  }
}

function startPolling(docId) {
  if (pollTimers[docId]) return
  pollTimers[docId] = setInterval(async () => {
    try {
      const res = await getLatestJob(docId)
      const job = res.data?.data || res.data
      const idx = documents.value.findIndex(d => d.id === docId)
      if (idx !== -1) {
        documents.value[idx].jobInfo = job
        if (job && job.jobStatus === 'COMPLETED') {
          documents.value[idx].documentStatus = 'INDEXED'
          clearInterval(pollTimers[docId])
          delete pollTimers[docId]
          showToast('success', `✅ 索引完成！共 ${job.totalChunks} 个分块已入库`)
        } else if (job && job.jobStatus === 'FAILED') {
          documents.value[idx].documentStatus = 'ERROR'
          documents.value[idx].errorMessage = job.errorMessage
          clearInterval(pollTimers[docId])
          delete pollTimers[docId]
          showToast('error', '❌ 索引失败: ' + (job.errorMessage || '未知错误'))
        }
      }
    } catch (e) {
      // ignore polling errors
    }
  }, 1500)
}

onMounted(async () => {
  try {
    const res = await listDocuments()
    const data = res.data?.data || res.data
    documents.value = data?.records || data || []
    // Resume polling for in-progress docs
    documents.value.forEach(doc => {
      const status = doc.documentStatus || doc.status
      if (status === 'INDEXING') {
        startPolling(doc.id)
      }
    })
  } catch (e) {
    // Backend not running
  }
})

onUnmounted(() => {
  Object.values(pollTimers).forEach(clearInterval)
})
</script>
