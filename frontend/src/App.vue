<template>
  <!-- Login page: no sidebar -->
  <div v-if="$route.name === 'Login'" style="height: 100%;">
    <router-view />
  </div>

  <!-- Main layout with sidebar -->
  <div v-else class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">📖</div>
        <h1>Novel RAG</h1>
      </div>

      <!-- Navigation -->
      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          <span class="nav-icon">💬</span>
          <span>智能问答</span>
        </router-link>
        <router-link to="/upload" class="nav-item" :class="{ active: $route.path === '/upload' }">
          <span class="nav-icon">📄</span>
          <span>文档管理</span>
        </router-link>
        <router-link to="/settings" class="nav-item" :class="{ active: $route.path === '/settings' }">
          <span class="nav-icon">⚙️</span>
          <span>模型设置</span>
        </router-link>
      </nav>

      <!-- Chat Sessions (only on chat page) -->
      <div class="sessions-section" v-if="$route.path === '/'">
        <div class="sessions-header">
          <span class="sessions-title">对话列表</span>
          <button class="btn-icon" @click="showNewSessionDialog = true" title="新对话" id="new-session-btn">＋</button>
        </div>
        <div class="sessions-list" v-if="sessions.length > 0">
          <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: s.id === activeSessionId }"
            @click="selectSession(s.id)"
            :id="'session-' + s.id"
          >
            <span class="session-icon">💬</span>
            <div class="session-info">
              <span class="session-title">{{ s.title }}</span>
              <span class="session-doc-count" v-if="s.documentIds && s.documentIds.length > 0">
                📚 {{ s.documentIds.length }} 篇文档
              </span>
              <span class="session-doc-count" v-else>
                📚 全部文档
              </span>
            </div>
          </div>
        </div>
        <div v-else class="sessions-empty">暂无对话</div>
      </div>

      <!-- User info + Logout -->
      <div class="sidebar-footer">
        <div class="user-info" v-if="auth.user">
          <div class="user-avatar">{{ (auth.user.displayName || auth.user.username || '?')[0] }}</div>
          <div class="user-detail">
            <div class="user-name">{{ auth.user.displayName || auth.user.username }}</div>
            <div class="user-role">{{ auth.user.roleCode }}</div>
          </div>
        </div>
        <button class="btn btn-secondary btn-sm" @click="handleLogout" id="logout-btn" style="width: 100%; margin-top: 8px;">
          退出登录
        </button>
      </div>
    </aside>

    <main class="main-content">
      <router-view
        :activeSessionId="activeSessionId"
        @update:activeSessionId="activeSessionId = $event"
        @sessions-changed="loadSessions"
      />
    </main>

    <!-- New Session Dialog -->
    <div v-if="showNewSessionDialog" class="dialog-overlay" @click.self="showNewSessionDialog = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>📝 新建对话</h3>
          <button class="btn-icon" @click="showNewSessionDialog = false">✕</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>对话标题</label>
            <input
              v-model="newSessionTitle"
              class="form-input"
              placeholder="例如：雷雨人物分析"
              @keydown.enter="confirmNewSession"
            />
          </div>
          <div class="form-group">
            <label>选择文本库（可多选）</label>
            <div class="doc-select-hint">不选则查询全部已索引文档</div>
            <div class="doc-select-list">
              <div
                v-for="doc in indexedDocuments"
                :key="doc.id"
                class="doc-select-item"
                :class="{ selected: selectedDocIds.includes(doc.id) }"
                @click="toggleDocSelect(doc.id)"
              >
                <span class="doc-check">{{ selectedDocIds.includes(doc.id) ? '☑' : '☐' }}</span>
                <span class="doc-select-name">{{ doc.fileName }}</span>
                <span class="doc-select-size">{{ formatSize(doc.fileSizeBytes) }}</span>
              </div>
              <div v-if="indexedDocuments.length === 0" class="doc-select-empty">
                暂无已索引文档，请先在「文档管理」中上传并索引文档。
              </div>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="showNewSessionDialog = false">取消</button>
          <button class="btn btn-primary" @click="confirmNewSession" :disabled="!newSessionTitle.trim()">
            创建对话
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { auth, clearAuth } from './auth'
import { listSessions, createSession, listDocuments } from './api'

const router = useRouter()
const route = useRoute()
const sessions = ref([])
const activeSessionId = ref(null)

// New session dialog state
const showNewSessionDialog = ref(false)
const newSessionTitle = ref('')
const selectedDocIds = ref([])
const indexedDocuments = ref([])

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function loadSessions() {
  if (!auth.isLoggedIn) return
  try {
    const res = await listSessions()
    sessions.value = res.data?.data || res.data || []
    // Auto-select the first session if none selected
    if (!activeSessionId.value && sessions.value.length > 0) {
      activeSessionId.value = sessions.value[0].id
    }
  } catch (e) {
    // may fail if backend is down
  }
}

async function loadIndexedDocuments() {
  try {
    const res = await listDocuments(1, 100)
    const data = res.data?.data || res.data
    const docs = data?.records || data || []
    indexedDocuments.value = docs.filter(d =>
      (d.documentStatus || d.status) === 'INDEXED'
    )
  } catch (e) {
    indexedDocuments.value = []
  }
}

function selectSession(id) {
  activeSessionId.value = id
}

function toggleDocSelect(docId) {
  const idx = selectedDocIds.value.indexOf(docId)
  if (idx === -1) {
    selectedDocIds.value.push(docId)
  } else {
    selectedDocIds.value.splice(idx, 1)
  }
}

async function confirmNewSession() {
  const title = newSessionTitle.value.trim()
  if (!title) return
  try {
    const docIds = selectedDocIds.value.length > 0 ? selectedDocIds.value : null
    const res = await createSession(title, docIds)
    const session = res.data?.data || res.data
    // Add documentIds to local session object for sidebar display
    session.documentIds = docIds || []
    sessions.value.unshift(session)
    activeSessionId.value = session.id
    showNewSessionDialog.value = false
    newSessionTitle.value = ''
    selectedDocIds.value = []
  } catch (e) {
    console.error('Failed to create session', e)
  }
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}

onMounted(() => {
  if (auth.isLoggedIn) {
    loadSessions()
    loadIndexedDocuments()
  }
})

watch(() => route.path, (newPath) => {
  if (newPath === '/' && auth.isLoggedIn) {
    loadSessions()
    loadIndexedDocuments()
  }
})

watch(showNewSessionDialog, (val) => {
  if (val) {
    newSessionTitle.value = '新对话 ' + new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    selectedDocIds.value = []
    loadIndexedDocuments()
  }
})
</script>

<style scoped>
/* New Session Dialog */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.dialog-box {
  background: var(--bg-secondary, #1e1e2e);
  border-radius: 16px;
  width: 480px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 12px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary, #fff);
}

.dialog-body {
  padding: 8px 24px 20px;
  overflow-y: auto;
  flex: 1;
}

.dialog-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #ccc);
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary, #fff);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: var(--accent-primary, #7c3aed);
}

.doc-select-hint {
  font-size: 12px;
  color: var(--text-secondary, #888);
  margin-bottom: 8px;
}

.doc-select-list {
  max-height: 240px;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.2);
}

.doc-select-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  transition: background 0.15s;
  gap: 8px;
}

.doc-select-item:last-child {
  border-bottom: none;
}

.doc-select-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.doc-select-item.selected {
  background: rgba(124, 58, 237, 0.15);
}

.doc-check {
  font-size: 16px;
  width: 20px;
  flex-shrink: 0;
}

.doc-select-name {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary, #ccc);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-select-size {
  font-size: 12px;
  color: var(--text-secondary, #888);
  flex-shrink: 0;
}

.doc-select-empty {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary, #888);
}

/* Session doc count in sidebar */
.session-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
}

.session-doc-count {
  font-size: 11px;
  color: var(--text-secondary, #888);
  margin-top: 2px;
}
</style>
