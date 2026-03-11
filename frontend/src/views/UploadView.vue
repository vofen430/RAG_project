<template>
  <div>
    <div class="page-header">
      <h2>📄 文档管理</h2>
      <p>上传小说文本文件，系统将自动分块、嵌入并索引</p>
    </div>
    <div class="page-body fade-in">
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
        <input type="file" ref="fileInput" accept=".txt" style="display:none" @change="handleFileSelect" />
        <span class="upload-icon">📤</span>
        <h3>拖拽文件到此处或点击上传</h3>
        <p>支持 .txt 文件，自动检测 UTF-8 / GBK 编码</p>
      </div>

      <!-- Upload Progress -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="card" style="margin-top: 16px;">
        <div class="card-title">⬆️ 上传中...</div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
        </div>
      </div>

      <!-- Document List -->
      <div class="doc-list" v-if="documents.length > 0">
        <div class="card-title" style="margin-top: 24px; padding: 0;">📚 已上传文档</div>
        <div v-for="doc in documents" :key="doc.id" class="doc-item">
          <div class="doc-info">
            <div class="doc-icon">📖</div>
            <div>
              <div class="doc-name">{{ doc.fileName }}</div>
              <div class="doc-size">{{ formatSize(doc.fileSize) }} · {{ doc.totalChunks }} 个分块</div>
              <div class="progress-bar" v-if="doc.status === 'EMBEDDING' && doc.totalChunks > 0" style="width: 200px; margin-top: 4px;">
                <div class="progress-fill" :style="{ width: (doc.processedChunks / doc.totalChunks * 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 12px;">
            <span class="doc-status" :class="'status-' + doc.status.toLowerCase()">
              {{ statusLabels[doc.status] || doc.status }}
            </span>
            <button
              v-if="doc.status === 'UPLOADED'"
              class="btn btn-primary btn-sm"
              @click="startIndex(doc.id)"
              :id="'index-btn-' + doc.id"
            >
              开始索引
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

    <!-- Toast -->
    <div v-if="toast.show" :class="'toast toast-' + toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { uploadDocument, listDocuments, indexDocument, getDocumentStatus } from '../api'

const documents = ref([])
const dragging = ref(false)
const uploadProgress = ref(0)
const toast = ref({ show: false, type: 'success', message: '' })
const fileInput = ref(null)

let pollTimers = {}

const statusLabels = {
  UPLOADED: '已上传',
  CHUNKING: '分块中...',
  EMBEDDING: '嵌入中...',
  INDEXING: '索引中...',
  INDEXED: '已索引 ✓',
  ERROR: '错误'
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function showToast(type, message) {
  toast.value = { show: true, type, message }
  setTimeout(() => { toast.value.show = false }, 3000)
}

async function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) await upload(file)
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
    documents.value.push(res.data)
    uploadProgress.value = 0
    showToast('success', '文件上传成功!')
  } catch (err) {
    uploadProgress.value = 0
    showToast('error', '上传失败: ' + (err.response?.data?.error || err.message))
  }
}

async function startIndex(docId) {
  try {
    await indexDocument(docId)
    showToast('success', '索引已启动')
    startPolling(docId)
  } catch (err) {
    showToast('error', '索引启动失败: ' + err.message)
  }
}

function startPolling(docId) {
  if (pollTimers[docId]) return
  pollTimers[docId] = setInterval(async () => {
    try {
      const res = await getDocumentStatus(docId)
      const idx = documents.value.findIndex(d => d.id === docId)
      if (idx !== -1) {
        documents.value[idx] = res.data
      }
      if (['INDEXED', 'ERROR'].includes(res.data.status)) {
        clearInterval(pollTimers[docId])
        delete pollTimers[docId]
        if (res.data.status === 'INDEXED') {
          showToast('success', '索引完成! 共 ' + res.data.totalChunks + ' 个分块')
        } else {
          showToast('error', '索引失败: ' + res.data.errorMessage)
        }
      }
    } catch (e) {
      // ignore polling errors
    }
  }, 2000)
}

onMounted(async () => {
  try {
    const res = await listDocuments()
    documents.value = res.data || []
    // Resume polling for in-progress docs
    documents.value.forEach(doc => {
      if (['CHUNKING', 'EMBEDDING', 'INDEXING'].includes(doc.status)) {
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
