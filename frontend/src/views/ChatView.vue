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

    <!-- Main chat area -->
    <div class="chat-body">
      <!-- Messages panel -->
      <div class="chat-messages-panel">
        <div class="chat-messages" ref="messagesRef">
          <!-- No session selected -->
          <div v-if="!activeSessionId" class="empty-state">
            <div class="empty-icon">💬</div>
            <h3>开始新的对话</h3>
            <p>点击左侧「＋」创建一个新对话，然后提问关于小说人物关系的问题。</p>
            <button class="btn btn-primary" @click="$emit('sessions-changed')" style="margin-top: 16px;" id="create-first-session-btn">
              ＋ 创建对话
            </button>
          </div>

          <!-- Empty conversation -->
          <div v-else-if="messages.length === 0 && !isStreaming" class="empty-state">
            <div class="empty-icon">🔍</div>
            <h3>开始分析人物关系</h3>
            <p>上传小说文档后，在此提问关于人物关系、角色性格、情节发展等问题。</p>
          </div>

          <!-- Message list -->
          <div
            v-for="(msg, i) in messages"
            :key="msg.id || i"
            class="chat-message"
            :class="msg.role || msg.messageRole"
          >
            <div class="avatar">{{ (msg.role || msg.messageRole) === 'user' ? '👤' : '🤖' }}</div>
            <div class="message-bubble">
              <div class="message-content" v-html="renderMarkdown(msg.content || msg.contentText)"></div>
              <!-- Trace link for assistant messages -->
              <div class="message-meta" v-if="(msg.role || msg.messageRole) === 'assistant' && msg.traceId">
                <button class="trace-link" @click="showTrace(msg.traceId)" :id="'trace-btn-' + msg.traceId">
                  🔎 查看溯源
                </button>
                <button class="feedback-btn good" @click="sendFeedback(msg.traceId, 'THUMBS_UP')" title="有帮助">👍</button>
                <button class="feedback-btn bad" @click="sendFeedback(msg.traceId, 'THUMBS_DOWN')" title="无帮助">👎</button>
              </div>
            </div>
          </div>

          <!-- Streaming message -->
          <div v-if="isStreaming" class="chat-message assistant">
            <div class="avatar">🤖</div>
            <div class="message-bubble">
              <div class="message-content">
                <div v-html="renderMarkdown(streamingContent)"></div>
                <div class="typing-indicator" v-if="streamingContent === ''">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input-area" v-if="activeSessionId">
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

      <!-- Trace/Evidence side panel -->
      <transition name="panel-slide">
        <div class="trace-panel" v-if="traceData" id="trace-panel">
          <div class="trace-panel-header">
            <h3>🔎 溯源详情</h3>
            <button class="btn-icon" @click="traceData = null" id="close-trace-btn">✕</button>
          </div>

          <div class="trace-panel-body">
            <!-- Query info -->
            <div class="trace-section">
              <div class="trace-label">用户提问</div>
              <div class="trace-value">{{ traceData.userQuery }}</div>
            </div>

            <div class="trace-section" v-if="traceData.latencyMs">
              <div class="trace-label">响应耗时</div>
              <div class="trace-value">{{ traceData.latencyMs }} ms</div>
            </div>

            <!-- Evidence items -->
            <div class="trace-section">
              <div class="trace-label">引用来源 ({{ traceData.evidenceItems?.length || 0 }})</div>
            </div>

            <div
              v-for="item in traceData.evidenceItems"
              :key="item.citationNo"
              class="evidence-card"
            >
              <div class="evidence-header">
                <span class="citation-badge">[{{ item.citationNo }}]</span>
                <span class="evidence-doc">{{ item.documentName }}</span>
                <span class="evidence-score" v-if="item.rerankScore">
                  Score: {{ Number(item.rerankScore).toFixed(3) }}
                </span>
              </div>
              <div class="evidence-section" v-if="item.sectionLabel">
                {{ item.sectionLabel }}
              </div>
              <div class="evidence-text">
                {{ item.contentText ? (item.contentText.length > 300 ? item.contentText.slice(0, 300) + '...' : item.contentText) : '(加载中...)' }}
              </div>
            </div>

            <div v-if="!traceData.evidenceItems?.length" class="trace-empty">
              暂无引用证据
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { marked } from 'marked'
import { streamQuery, getMessages, getTrace, submitFeedback, createSession } from '../api'

const props = defineProps({
  activeSessionId: String,
})
const emit = defineEmits(['update:activeSessionId', 'sessions-changed'])

const query = ref('')
const messages = ref([])
const isStreaming = ref(false)
const streamingContent = ref('')
const messagesRef = ref(null)
const traceData = ref(null)
const currentTraceId = ref(null)

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

// Load messages when session changes
watch(() => props.activeSessionId, async (newId) => {
  if (!newId) {
    messages.value = []
    return
  }
  try {
    const res = await getMessages(newId)
    const data = res.data?.data || res.data
    messages.value = (data?.records || data || []).map(m => ({
      ...m,
      role: m.messageRole,
      content: m.contentText,
    }))
    scrollToBottom()
  } catch (e) {
    messages.value = []
  }
}, { immediate: true })

async function sendMessage() {
  const q = query.value.trim()
  if (!q || isStreaming.value) return

  let sessionId = props.activeSessionId

  // Auto-create session if none
  if (!sessionId) {
    try {
      const title = q.length > 20 ? q.slice(0, 20) + '...' : q
      const res = await createSession(title)
      const session = res.data?.data || res.data
      sessionId = session.id
      emit('update:activeSessionId', sessionId)
      emit('sessions-changed')
    } catch (e) {
      return
    }
  }

  // Add user message to UI
  messages.value.push({ role: 'user', content: q, id: 'temp-user-' + Date.now() })
  query.value = ''
  isStreaming.value = true
  streamingContent.value = ''
  currentTraceId.value = null
  scrollToBottom()

  // Pipeline animation
  activateStep(0)
  setTimeout(() => activateStep(1), 300)
  setTimeout(() => activateStep(2), 800)
  setTimeout(() => activateStep(3), 1500)
  setTimeout(() => activateStep(4), 2200)
  setTimeout(() => activateStep(5), 3000)

  await streamQuery(sessionId, q, null, {
    onToken: (token, traceId) => {
      streamingContent.value += token
      if (traceId) currentTraceId.value = traceId
      scrollToBottom()
    },
    onComplete: (payload) => {
      messages.value.push({
        role: 'assistant',
        content: streamingContent.value,
        traceId: payload.traceId,
        id: payload.messageId || 'msg-' + Date.now(),
      })
      streamingContent.value = ''
      isStreaming.value = false
      pipelineSteps.value.forEach(s => { s.active = false; s.completed = true })
      scrollToBottom()
    },
    onError: (errMsg) => {
      messages.value.push({
        role: 'assistant',
        content: '❌ 请求失败: ' + errMsg + '\n\n请检查是否已上传和索引文档，并在设置页配置 API Key。',
        id: 'err-' + Date.now(),
      })
      streamingContent.value = ''
      isStreaming.value = false
      resetPipeline()
      scrollToBottom()
    }
  })
}

async function showTrace(traceId) {
  try {
    const res = await getTrace(traceId)
    traceData.value = res.data?.data || res.data
  } catch (e) {
    console.error('Failed to load trace', e)
  }
}

async function sendFeedback(traceId, feedbackType) {
  try {
    await submitFeedback(traceId, feedbackType, '')
    // Visual confirmation
    const btn = document.querySelector(`.feedback-btn.${feedbackType === 'THUMBS_UP' ? 'good' : 'bad'}`)
    if (btn) {
      btn.style.opacity = '1'
      btn.style.transform = 'scale(1.2)'
      setTimeout(() => { btn.style.transform = 'scale(1)' }, 300)
    }
  } catch (e) {
    console.error('Failed to submit feedback', e)
  }
}
</script>
