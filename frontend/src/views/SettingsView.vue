<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>⚙️ 模型设置</h2>
      <p>根据 SiliconFlow 接口文档，配置 API Key、各阶段模型和 RAG 参数</p>
    </div>
    <div class="page-body">
      <div class="settings-scroll">

        <!-- API Key Card (Top priority) -->
        <div class="card api-key-card">
          <div class="card-title">🔑 SiliconFlow API Key</div>
          <p class="card-desc">
            访问 <a href="https://cloud.siliconflow.com/account/ak" target="_blank" class="link">SiliconFlow 控制台</a> 获取你的 API Key，所有模型调用都需要此密钥
          </p>
          <div class="input-group">
            <label>API Key</label>
            <div class="api-key-input-wrapper">
              <input
                :type="showApiKey ? 'text' : 'password'"
                class="input-field"
                v-model="apiKeyInput"
                placeholder="sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                id="api-key-input"
                autocomplete="off"
              />
              <button class="btn-icon eye-btn" @click="showApiKey = !showApiKey" type="button">
                {{ showApiKey ? '🙈' : '👁️' }}
              </button>
            </div>
          </div>
          <div class="api-key-status">
            <span v-if="hasApiKey" class="badge badge-success">✅ 已配置</span>
            <span v-else class="badge badge-warning">⚠️ 未配置 — 模型调用将失败</span>
            <span v-if="apiKeyMasked" class="api-key-masked">当前: {{ apiKeyMasked }}</span>
          </div>
        </div>

        <div class="settings-grid">
          <!-- Embedding Model -->
          <div class="card">
            <div class="card-title">🔢 嵌入模型 (Embedding)</div>
            <p class="card-desc">调用 SiliconFlow <code>POST /embeddings</code> 接口，将文本转化为向量</p>
            <div class="model-selector">
              <div class="input-group">
                <label>选择模型</label>
                <select class="input-field" v-model="settings.embeddingModel" :disabled="customInputs.embedding" id="embedding-model-select">
                  <option v-for="opt in modelOptions.embedding" :key="opt.id" :value="opt.id">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <label class="custom-toggle">
                <input type="checkbox" v-model="customInputs.embedding" />
                自定义模型名称
              </label>
              <div class="input-group" v-if="customInputs.embedding" style="margin-top: 8px;">
                <input class="input-field" v-model="settings.embeddingModel" placeholder="输入模型名称，如 BAAI/bge-m3" id="embedding-custom-input" />
              </div>
            </div>
            <div class="model-info" v-if="currentEmbeddingInfo">
              <span>最大输入: {{ currentEmbeddingInfo.maxTokens }} tokens</span>
            </div>
          </div>

          <!-- Reranking Model -->
          <div class="card">
            <div class="card-title">📊 重排序模型 (Reranking)</div>
            <p class="card-desc">调用 SiliconFlow <code>POST /rerank</code> 接口，对检索结果精排</p>
            <div class="model-selector">
              <div class="input-group">
                <label>选择模型</label>
                <select class="input-field" v-model="settings.rerankModel" :disabled="customInputs.reranking" id="reranking-model-select">
                  <option v-for="opt in modelOptions.reranking" :key="opt.id" :value="opt.id">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <label class="custom-toggle">
                <input type="checkbox" v-model="customInputs.reranking" />
                自定义模型名称
              </label>
              <div class="input-group" v-if="customInputs.reranking" style="margin-top: 8px;">
                <input class="input-field" v-model="settings.rerankModel" placeholder="输入模型名称" id="reranking-custom-input" />
              </div>
            </div>
          </div>

          <!-- Chat Model -->
          <div class="card">
            <div class="card-title">🤖 生成模型 (Chat Completions)</div>
            <p class="card-desc">调用 SiliconFlow <code>POST /chat/completions</code> 接口，SSE 流式生成回答</p>
            <div class="model-selector">
              <div class="input-group">
                <label>选择模型</label>
                <select class="input-field" v-model="settings.chatModel" :disabled="customInputs.chat" id="chat-model-select">
                  <option v-for="opt in modelOptions.chat" :key="opt.id" :value="opt.id">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <label class="custom-toggle">
                <input type="checkbox" v-model="customInputs.chat" />
                自定义模型名称
              </label>
              <div class="input-group" v-if="customInputs.chat" style="margin-top: 8px;">
                <input class="input-field" v-model="settings.chatModel" placeholder="输入模型名称，如 deepseek-ai/DeepSeek-V3" id="chat-custom-input" />
              </div>
            </div>
          </div>

          <!-- RAG Parameters -->
          <div class="card">
            <div class="card-title">🔧 RAG 参数</div>
            <div class="input-group">
              <label>分块大小 (字符)</label>
              <input type="number" class="input-field" v-model.number="settings.chunkSize" min="100" max="2000" id="chunk-size-input" />
            </div>
            <div class="input-group">
              <label>分块重叠 (字符)</label>
              <input type="number" class="input-field" v-model.number="settings.chunkOverlap" min="0" max="500" id="chunk-overlap-input" />
            </div>
            <div class="input-group">
              <label>Top-K 检索数量</label>
              <input type="number" class="input-field" v-model.number="settings.topK" min="1" max="50" id="top-k-input" />
              <span class="param-hint">向量检索返回的候选分块数</span>
            </div>
            <div class="input-group">
              <label>Top-N 重排序保留 (rerank top_n)</label>
              <input type="number" class="input-field" v-model.number="settings.topN" min="1" max="20" id="top-n-input" />
              <span class="param-hint">对应 SiliconFlow rerank 接口的 top_n 参数</span>
            </div>
          </div>
        </div>

        <!-- API Info -->
        <div class="card" style="margin-top: 24px;">
          <div class="card-title">📡 SiliconFlow API</div>
          <div class="api-info-grid">
            <div class="api-info-item">
              <span class="api-label">Base URL</span>
              <code>https://api.siliconflow.com/v1</code>
            </div>
            <div class="api-info-item">
              <span class="api-label">Embeddings</span>
              <code>POST /embeddings</code>
            </div>
            <div class="api-info-item">
              <span class="api-label">Rerank</span>
              <code>POST /rerank</code>
            </div>
            <div class="api-info-item">
              <span class="api-label">Chat</span>
              <code>POST /chat/completions</code> (stream: true)
            </div>
          </div>
        </div>

        <!-- Save All -->
        <div class="settings-actions">
          <button class="btn btn-primary" @click="saveAll" :disabled="saving" id="save-all-btn">
            <span v-if="saving" class="spinner" style="width:14px;height:14px;border-width:2px;"></span>
            <span v-else>💾 保存所有设置</span>
          </button>
          <span v-if="saveStatus" :style="{ color: saveStatus === 'success' ? 'var(--success)' : 'var(--error)', fontSize: '14px', lineHeight: '40px' }">
            {{ saveStatus === 'success' ? '✅ 设置已保存' : '❌ 保存失败' }}
          </span>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { getSettings, updateSettings, getModelOptions } from '../api'

const saving = ref(false)
const saveStatus = ref('')
const showApiKey = ref(false)
const apiKeyInput = ref('')
const apiKeyMasked = ref('')
const hasApiKey = ref(false)

const settings = reactive({
  embeddingModel: 'Qwen/Qwen3-Embedding-0.6B',
  rerankModel: 'Qwen/Qwen3-Reranker-0.6B',
  chatModel: 'Qwen/Qwen3-8B',
  chunkSize: 500,
  chunkOverlap: 100,
  topK: 10,
  topN: 5
})

const customInputs = reactive({
  embedding: false,
  reranking: false,
  chat: false
})

const modelOptions = reactive({
  embedding: [],
  reranking: [],
  chat: []
})

const currentEmbeddingInfo = computed(() => {
  return modelOptions.embedding.find(o => o.id === settings.embeddingModel) || null
})

async function saveAll() {
  saving.value = true
  try {
    const payload = {
      embeddingModel: settings.embeddingModel,
      rerankModel: settings.rerankModel,
      chatModel: settings.chatModel,
      chunkSize: settings.chunkSize,
      chunkOverlap: settings.chunkOverlap,
      topK: settings.topK,
      topN: settings.topN
    }
    // Only include apiKey if user typed something new
    if (apiKeyInput.value && !apiKeyInput.value.includes('••••')) {
      payload.apiKey = apiKeyInput.value
    }
    const res = await updateSettings(payload)
    const data = res.data?.data || res.data
    if (data) {
      apiKeyMasked.value = data.apiKeyMasked || ''
      hasApiKey.value = data.hasApiKey || false
      apiKeyInput.value = '' // Clear after save
    }
    saveStatus.value = 'success'
    setTimeout(() => saveStatus.value = '', 3000)
  } catch (e) {
    saveStatus.value = 'error'
    setTimeout(() => saveStatus.value = '', 3000)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  // Load model options from backend
  try {
    const modelsRes = await getModelOptions()
    const data = modelsRes.data?.data || modelsRes.data
    if (data) {
      for (const stage of ['embedding', 'reranking', 'chat']) {
        if (data[stage]?.options) {
          modelOptions[stage] = data[stage].options
        }
      }
    }
  } catch (e) {
    console.warn('Failed to load model options', e)
  }

  // Load current user settings
  try {
    const res = await getSettings()
    const data = res.data?.data || res.data
    if (data) {
      settings.embeddingModel = data.embeddingModel || settings.embeddingModel
      settings.rerankModel = data.rerankModel || settings.rerankModel
      settings.chatModel = data.chatModel || settings.chatModel
      settings.chunkSize = data.chunkSize ?? settings.chunkSize
      settings.chunkOverlap = data.chunkOverlap ?? settings.chunkOverlap
      settings.topK = data.topK ?? settings.topK
      settings.topN = data.topN ?? settings.topN

      // API key state
      apiKeyMasked.value = data.apiKeyMasked || ''
      hasApiKey.value = data.hasApiKey || false

      // Check if current model is in the options list
      if (!modelOptions.embedding.find(o => o.id === settings.embeddingModel) && modelOptions.embedding.length > 0) {
        customInputs.embedding = true
      }
      if (!modelOptions.reranking.find(o => o.id === settings.rerankModel) && modelOptions.reranking.length > 0) {
        customInputs.reranking = true
      }
      if (!modelOptions.chat.find(o => o.id === settings.chatModel) && modelOptions.chat.length > 0) {
        customInputs.chat = true
      }
    }
  } catch (e) {
    // Use defaults
  }
})
</script>
