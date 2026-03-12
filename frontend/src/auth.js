import { reactive } from 'vue'

const AUTH_KEY = 'rag_auth'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(AUTH_KEY)
    if (raw) return JSON.parse(raw)
  } catch {}
  return null
}

const stored = loadFromStorage()

export const auth = reactive({
  token: stored?.token || null,
  user: stored?.user || null,
  get isLoggedIn() {
    return !!this.token
  }
})

export function setAuth(token, user) {
  auth.token = token
  auth.user = user
  localStorage.setItem(AUTH_KEY, JSON.stringify({ token, user }))
}

export function clearAuth() {
  auth.token = null
  auth.user = null
  localStorage.removeItem(AUTH_KEY)
}

export function getToken() {
  return auth.token
}
