<template>
  <div class="login-page">
    <div class="login-bg-gradient"></div>
    <div class="login-card fade-in">
      <div class="login-logo">
        <div class="logo-icon-lg">📖</div>
        <h1>Novel RAG</h1>
        <p class="login-subtitle">小说人物关系分析系统</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <label>用户名</label>
          <input
            type="text"
            class="input-field"
            v-model="username"
            placeholder="请输入用户名"
            autocomplete="username"
            id="login-username"
          />
        </div>
        <div class="input-group">
          <label>密码</label>
          <input
            type="password"
            class="input-field"
            v-model="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            id="login-password"
          />
        </div>

        <div v-if="errorMsg" class="login-error">
          <span>⚠️</span> {{ errorMsg }}
        </div>

        <button
          type="submit"
          class="btn btn-primary login-btn"
          :disabled="loading || !username.trim() || !password.trim()"
          id="login-submit"
        >
          <span v-if="loading" class="spinner" style="width:16px;height:16px;border-width:2px;"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="login-hint">
        <span>默认账号</span>
        <code>demo</code> / <code>demo-password</code>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as apiLogin } from '../api'
import { setAuth } from '../auth'

const router = useRouter()
const username = ref('demo')
const password = ref('demo-password')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  errorMsg.value = ''

  try {
    const res = await apiLogin(username.value, password.value)
    const data = res.data?.data || res.data
    setAuth(data.accessToken, data.user)
    router.push('/')
  } catch (err) {
    const msg = err.response?.data?.message || err.message || '登录失败'
    errorMsg.value = msg
  } finally {
    loading.value = false
  }
}
</script>
