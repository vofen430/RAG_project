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
          <button class="btn-icon" @click="handleNewSession" title="新对话" id="new-session-btn">＋</button>
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
            <span class="session-title">{{ s.title }}</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { auth, clearAuth } from './auth'
import { listSessions, createSession } from './api'

const router = useRouter()
const route = useRoute()
const sessions = ref([])
const activeSessionId = ref(null)

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

function selectSession(id) {
  activeSessionId.value = id
}

async function handleNewSession() {
  try {
    const title = '新对话 ' + new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    const res = await createSession(title)
    const session = res.data?.data || res.data
    sessions.value.unshift(session)
    activeSessionId.value = session.id
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
  }
})

watch(() => route.path, (newPath) => {
  if (newPath === '/' && auth.isLoggedIn) {
    loadSessions()
  }
})
</script>
