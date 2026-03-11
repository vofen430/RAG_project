import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 120000,
})

// Documents
export const uploadDocument = (file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress,
  })
}

export const listDocuments = () => api.get('/documents')

export const indexDocument = (id) => api.post(`/documents/${id}/index`)

export const getDocumentStatus = (id) => api.get(`/documents/${id}/status`)

// Chat (streaming via fetch for SSE)
export const streamChat = async (query, onToken, onDone, onError) => {
  try {
    const response = await fetch('http://localhost:8080/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query }),
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const text = decoder.decode(value, { stream: true })
      // SSE format: "data:token\n\n"
      const lines = text.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const token = line.slice(5)
          if (token && token !== '') {
            onToken(token)
          }
        }
      }
    }

    onDone()
  } catch (err) {
    onError(err)
  }
}

export const getChatHistory = () => api.get('/chat/history')
export const clearChatHistory = () => api.delete('/chat/history')

// Settings
export const getModels = () => api.get('/settings/models')

export const updateModels = (selections) => api.put('/settings/models', selections)

export const updateApiKey = (apiKey) => api.put('/settings/api-key', { apiKey })

export const getParameters = () => api.get('/settings/parameters')

export const updateParameters = (params) => api.put('/settings/parameters', params)
