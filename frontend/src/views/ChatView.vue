<template>
  <div class="chat-container">
    <!-- Pipeline Visualizer -->
    <div class="pipeline-vis" v-if="pipelineSteps.length">
      <template v-for="(step, i) in pipelineSteps" :key="step.name">
        <div class="pipeline-step" :class="{ active: step.active, completed: step.completed }">
          <span>{{ step.icon }}</span>
          <span>{{ step.name }}</span>
        </div>
        <span class="pipeline-arrow" v-if="i < pipelineSteps.length - 1">→</span>
      </template>
    </div>

    <!-- Messages -->
    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3>开始分析人物关系</h3>
        <p>上传小说文档后，在此提问关于人物关系、角色性格、情节发展等问题。</p>
      </div>
      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="chat-message"
        :class="msg.role"
      >
        <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
        <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
      </div>
      <div v-if="isStreaming" class="chat-message assistant">
        <div class="avatar">🤖</div>
        <div class="message-content">
          <div v-html="renderMarkdown(streamingContent)"></div>
          <div class="typing-indicator" v-if="streamingContent === ''">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="chat-input-area">
      <div class="chat-input-wrapper">
        <textarea
          v-model="query"
          class="chat-input"
          placeholder="请输入关于小说人物关系的问题，例如：周朴园和鲁侍萍之间的关系是什么？"
          @keydown.enter.exact.prevent="sendMessage"
          rows="1"
          id="chat-input"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="!query.trim() || isStreaming" id="send-btn">
          ▶
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import { streamChat, getChatHistory } from '../api'

const query = ref('')
const messages = ref([])
const isStreaming = ref(false)
const streamingContent = ref('')
const messagesRef = ref(null)

const pipelineSteps = ref([
  { name: '用户查询', icon: '💬', active: false, completed: false },
  { name: '查询嵌入', icon: '🔢', active: false, completed: false },
  { name: 'Top-K检索', icon: '🔍', active: false, completed: false },
  { name: '重排序', icon: '📊', active: false, completed: false },
  { name: '提示构建', icon: '📝', active: false, completed: false },
  { name: 'LLM生成', icon: '🤖', active: false, completed: false },
])

function renderMarkdown(text) {
  if (!text) return ''
  try {
    return marked.parse(text, { breaks: true })
  } catch {
    return text
  }
}

function resetPipeline() {
  pipelineSteps.value.forEach(s => { s.active = false; s.completed = false })
}

function activateStep(index) {
  pipelineSteps.value.forEach((s, i) => {
    if (i < index) { s.active = false; s.completed = true }
    else if (i === index) { s.active = true; s.completed = false }
    else { s.active = false; s.completed = false }
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const q = query.value.trim()
  if (!q || isStreaming.value) return

  messages.value.push({ role: 'user', content: q })
  query.value = ''
  isStreaming.value = true
  streamingContent.value = ''
  scrollToBottom()

  // Simulate pipeline progression
  activateStep(0) // User Query
  setTimeout(() => activateStep(1), 300) // Query Embedding
  setTimeout(() => activateStep(2), 800) // Top-K Retrieval
  setTimeout(() => activateStep(3), 1500) // Reranking
  setTimeout(() => activateStep(4), 2200) // Prompt Construction
  setTimeout(() => activateStep(5), 3000) // LLM Generation

  await streamChat(
    q,
    (token) => {
      streamingContent.value += token
      scrollToBottom()
    },
    () => {
      messages.value.push({ role: 'assistant', content: streamingContent.value })
      streamingContent.value = ''
      isStreaming.value = false
      pipelineSteps.value.forEach(s => { s.active = false; s.completed = true })
      scrollToBottom()
    },
    (err) => {
      messages.value.push({ role: 'assistant', content: '❌ 请求失败: ' + err.message + '\n\n请检查是否已在设置页面配置API Key，并确认已上传和索引文档。' })
      streamingContent.value = ''
      isStreaming.value = false
      resetPipeline()
      scrollToBottom()
    }
  )
}

onMounted(async () => {
  try {
    const res = await getChatHistory()
    if (res.data && res.data.length) {
      messages.value = res.data
    }
  } catch (e) {
    // No history
  }
})
</script>
