import axios from 'axios'
import { getToken, clearAuth } from '../auth'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 120000,
})

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401 globally
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ─── Auth ────────────────────────────────────
export const login = (username, password) =>
  api.post('/auth/login', { username, password })

export const getCurrentUser = () => api.get('/auth/me')

// ─── Documents ───────────────────────────────
export const uploadDocument = (file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress,
  })
}

export const listDocuments = (page = 1, size = 50) =>
  api.get('/documents', { params: { page, size } })

export const getDocument = (id) => api.get(`/documents/${id}`)

export const indexDocument = (id) => api.post(`/documents/${id}/index`)

export const getLatestJob = (id) => api.get(`/documents/${id}/jobs/latest`)

// ─── Chat Sessions ───────────────────────────
export const createSession = (title) =>
  api.post('/chat/sessions', { title })

export const listSessions = () => api.get('/chat/sessions')

export const getMessages = (sessionId, page = 1, size = 100) =>
  api.get(`/chat/sessions/${sessionId}/messages`, { params: { page, size } })

// ─── Streaming Chat ──────────────────────────
export async function streamQuery(sessionId, query, documentIds, callbacks) {
  const { onToken, onComplete, onError } = callbacks
  const token = getToken()

  try {
    const response = await fetch(
      `http://localhost:8080/api/chat/sessions/${sessionId}/query/stream`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ query, documentIds }),
      }
    )

    if (!response.ok) {
      if (response.status === 401) {
        clearAuth()
        window.location.href = '/login'
        return
      }
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // Parse SSE events from buffer
      const parts = buffer.split('\n\n')
      buffer = parts.pop() // keep incomplete

      for (const part of parts) {
        const lines = part.split('\n')
        let eventType = 'message'
        let data = ''

        for (const line of lines) {
          if (line.startsWith('event: ')) {
            eventType = line.slice(7).trim()
          } else if (line.startsWith('data: ')) {
            data = line.slice(6)
          }
        }

        if (!data) continue

        try {
          const payload = JSON.parse(data)

          if (eventType === 'token') {
            onToken(payload.content || '', payload.traceId)
          } else if (eventType === 'complete') {
            onComplete(payload)
          } else if (eventType === 'error') {
            onError(payload.error || 'Unknown error')
          }
        } catch {
          // Non-JSON data, treat as raw token
          if (eventType === 'token') {
            onToken(data)
          }
        }
      }
    }
  } catch (err) {
    onError(err.message || 'Stream connection failed')
  }
}

// ─── Traces ──────────────────────────────────
export const getTrace = (traceId) => api.get(`/chat/traces/${traceId}`)

export const submitFeedback = (traceId, feedbackType, feedbackText) =>
  api.post(`/chat/traces/${traceId}/feedback`, { feedbackType, feedbackText })

// ─── Settings ────────────────────────────────
export const getSettings = () => api.get('/settings')

export const updateSettings = (settings) => api.put('/settings', settings)

export const getModelOptions = () => api.get('/settings/models')

// ─── Health ──────────────────────────────────
export const getHealth = () => api.get('/health')

