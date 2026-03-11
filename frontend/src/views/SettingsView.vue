<template>
  <div>
    <div class="page-header">
      <h2>⚙️ 模型设置</h2>
      <p>配置 API 密钥、选择各阶段使用的模型和调整 RAG 参数</p>
    </div>
    <div class="page-body fade-in">
      <div class="settings-grid">
        <!-- API Key -->
        <div class="card">
          <div class="card-title">🔑 API 密钥</div>
          <div class="input-group">
            <label>SiliconFlow API Key</label>
            <div style="display: flex; gap: 8px;">
              <input
                :type="showKey ? 'text' : 'password'"
                class="input-field"
                v-model="apiKey"
                placeholder="sk-..."
                id="api-key-input"
              />
              <button class="btn btn-secondary btn-sm" @click="showKey = !showKey">
                {{ showKey ? '🙈' : '👁️' }}
              </button>
            </div>
          </div>
          <button class="btn btn-primary btn-sm" @click="saveApiKey" id="save-api-key-btn">保存密钥</button>
        </div>

        <!-- Embedding Model -->
        <div class="card">
          <div class="card-title">🔢 嵌入模型 (Embedding)</div>
          <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px;">用于将文本转化为向量，影响检索质量</p>
          <div class="model-selector">
            <div class="input-group">
              <label>选择模型</label>
              <select class="input-field" v-model="selectedModels.embedding" :disabled="customInputs.embedding" id="embedding-model-select">
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
              <input class="input-field" v-model="selectedModels.embedding" placeholder="输入模型名称，如 BAAI/bge-m3" id="embedding-custom-input" />
            </div>
          </div>
        </div>

        <!-- Reranking Model -->
        <div class="card">
          <div class="card-title">📊 重排序模型 (Reranking)</div>
          <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px;">对检索结果进行精排，提高相关性</p>
          <div class="model-selector">
            <div class="input-group">
              <label>选择模型</label>
              <select class="input-field" v-model="selectedModels.reranking" :disabled="customInputs.reranking" id="reranking-model-select">
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
              <input class="input-field" v-model="selectedModels.reranking" placeholder="输入模型名称" id="reranking-custom-input" />
            </div>
          </div>
        </div>

        <!-- Chat Model -->
        <div class="card">
          <div class="card-title">🤖 生成模型 (LLM)</div>
          <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px;">用于最终回答生成，影响分析质量</p>
          <div class="model-selector">
            <div class="input-group">
              <label>选择模型</label>
              <select class="input-field" v-model="selectedModels.chat" :disabled="customInputs.chat" id="chat-model-select">
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
              <input class="input-field" v-model="selectedModels.chat" placeholder="输入模型名称，如 deepseek-ai/DeepSeek-V3" id="chat-custom-input" />
            </div>
          </div>
        </div>

        <!-- RAG Parameters -->
        <div class="card">
          <div class="card-title">🔧 RAG 参数</div>
          <div class="input-group">
            <label>分块大小 (字符)</label>
            <input type="number" class="input-field" v-model.number="params.chunkSize" min="100" max="2000" id="chunk-size-input" />
          </div>
          <div class="input-group">
            <label>分块重叠 (字符)</label>
            <input type="number" class="input-field" v-model.number="params.chunkOverlap" min="0" max="500" id="chunk-overlap-input" />
          </div>
          <div class="input-group">
            <label>Top-K 检索数量</label>
            <input type="number" class="input-field" v-model.number="params.topK" min="1" max="50" id="top-k-input" />
          </div>
          <div class="input-group">
            <label>Top-N 重排序保留</label>
            <input type="number" class="input-field" v-model.number="params.topN" min="1" max="20" id="top-n-input" />
          </div>
        </div>
      </div>

      <!-- Save All -->
      <div style="margin-top: 24px; display: flex; gap: 12px;">
        <button class="btn btn-primary" @click="saveAll" id="save-all-btn">💾 保存所有设置</button>
        <span v-if="saveStatus" :style="{ color: saveStatus === 'success' ? 'var(--success)' : 'var(--error)', fontSize: '14px', lineHeight: '40px' }">
          {{ saveStatus === 'success' ? '✅ 设置已保存' : '❌ 保存失败' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getModels, updateModels, updateApiKey, getParameters, updateParameters } from '../api'

const apiKey = ref('')
const showKey = ref(false)
const saveStatus = ref('')

const selectedModels = reactive({
  embedding: '',
  reranking: '',
  chat: ''
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

const params = reactive({
  chunkSize: 500,
  chunkOverlap: 100,
  topK: 10,
  topN: 5
})

async function saveApiKey() {
  try {
    await updateApiKey(apiKey.value)
    saveStatus.value = 'success'
    setTimeout(() => saveStatus.value = '', 3000)
  } catch (e) {
    saveStatus.value = 'error'
  }
}

async function saveAll() {
  try {
    await updateModels({
      embedding: selectedModels.embedding,
      reranking: selectedModels.reranking,
      chat: selectedModels.chat
    })
    await updateParameters({
      chunkSize: params.chunkSize,
      chunkOverlap: params.chunkOverlap,
      topK: params.topK,
      topN: params.topN
    })
    saveStatus.value = 'success'
    setTimeout(() => saveStatus.value = '', 3000)
  } catch (e) {
    saveStatus.value = 'error'
  }
}

onMounted(async () => {
  try {
    const modelsRes = await getModels()
    const data = modelsRes.data
    for (const stage of ['embedding', 'reranking', 'chat']) {
      if (data[stage]) {
        modelOptions[stage] = data[stage].options || []
        selectedModels[stage] = data[stage].selected || ''
      }
    }
  } catch (e) {
    // Backend not running, use defaults
  }

  try {
    const paramsRes = await getParameters()
    Object.assign(params, paramsRes.data)
  } catch (e) {
    // Use defaults
  }
})
</script>
