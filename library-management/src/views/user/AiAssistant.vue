<template>
  <div class="ai-assistant-view">
    <!-- 移动端侧边栏遮罩 -->
    <div v-if="sidebarVisible && isMobile" class="sidebar-overlay" @click="sidebarVisible = false" />

    <div class="ai-page">
    <!-- 移动端侧边栏切换按钮 -->
    <button class="mobile-toggle" @click="sidebarVisible = !sidebarVisible">
      <el-icon><Menu /></el-icon>
    </button>

    <aside class="ai-sidebar" :class="{ 'mobile-visible': sidebarVisible }">
      <div class="brand-block">
        <div class="brand-avatar">AI</div>
        <div>
          <h2>AI 智能助手</h2>
          <p>中文对话与思考</p>
        </div>
      </div>

      <!-- 新建对话（带动画） -->
      <button class="new-conv-btn" @click="startNewConversation">
        <div class="new-conv-inner">
          <el-icon class="new-conv-icon"><Plus /></el-icon>
          <span>新建对话</span>
        </div>
        <span class="new-conv-shimmer"></span>
      </button>

      <!-- 设置 + 状态合并行 -->
      <div
        class="settings-status-row"
        @mouseenter="statusHover = true"
        @mouseleave="statusHover = false"
        @click="openSettings"
      >
        <div class="ssr-left">
          <el-icon><Setting /></el-icon>
          <span>模型与API设置</span>
        </div>
        <div class="ssr-right">
          <span class="status-dot" :class="statusClass"></span>
          <span class="status-dot-label" :class="statusClass">{{ statusLabel }}</span>
        </div>
      </div>

      <!-- 悬浮展开状态详情 -->
      <transition name="slide-down">
        <div v-if="statusHover" class="status-expand">
          <span class="se-badge" :class="statusClass">
            <el-icon v-if="autoChecking" class="spin"><Loading /></el-icon>
            <el-icon v-else-if="statusClass === 'ok'"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            {{ statusLabel }}
          </span>
          <span class="se-model" :title="config.activeModelId">{{ config.activeModelId || '未配置' }}</span>
        </div>
      </transition>

      <!-- 历史会话列表 -->
      <div class="session-list-block">
        <div class="session-list-title">
          历史会话
          <span v-if="sessions.length" class="session-count">{{ sessions.length }}</span>
        </div>
        <div class="session-list" v-if="sessions.length > 0">
          <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: s.id === currentSessionId }"
            @click="loadSession(s.id)"
          >
            <el-icon class="session-icon"><ChatDotRound /></el-icon>
            <span class="session-title">{{ s.title }}</span>
            <el-icon class="session-del" @click.stop="deleteSession(s.id)"><Close /></el-icon>
          </div>
        </div>
        <div v-else class="session-empty">暂无历史会话</div>
      </div>

      <div class="tips-block">
        💡 设置 API Key → 检测 → 开始对话
      </div>
    </aside>

    <div class="ai-main-wrap">
    <section class="ai-main">
      <header class="chat-header">
        <div class="chat-title">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ currentSessionTitle || '智能对话' }}</span>
        </div>
        <div class="chat-actions">
          <el-button text @click="startNewConversation">
            <el-icon><RefreshRight /></el-icon>
            清空会话
          </el-button>
          <el-button text @click="openSettings">
            <el-icon><Setting /></el-icon>
            设置
          </el-button>
        </div>
      </header>

      <div ref="messagePanelRef" class="message-panel">
        <div v-if="messages.length === 0" class="empty-state">
          <el-icon :size="36"><MagicStick /></el-icon>
          <h3>你好，我是你的图书馆 AI 助手</h3>
          <p>你可以问我书籍推荐、阅读计划、学习方法、知识问答等问题。</p>
        </div>

        <div v-for="(item, index) in messages" :key="item.id" class="message-row" :class="item.role">
          <div class="avatar" :class="item.role">
            <el-icon v-if="item.role === 'assistant'"><MagicStick /></el-icon>
            <el-icon v-else><UserFilled /></el-icon>
          </div>

          <div class="bubble-wrap">
            <div class="bubble" :class="{ streaming: item.streaming, stopped: item.stopped }">
              <!-- 已停止标记 -->
              <div v-if="item.stopped" class="stopped-badge">
                <el-icon><VideoPause /></el-icon> 已停止生成
              </div>

              <!-- 思考块 -->
              <details v-if="item.thinking" class="thinking-block" :open="item.streaming">
                <summary class="thinking-summary">
                  <el-icon><Cpu /></el-icon>
                  深度思考
                  <span v-if="item.streaming && !item.content" class="thinking-badge">思考中...</span>
                </summary>
                <div class="thinking-content md-body" v-html="renderMarkdown(item.thinking)" />
              </details>

              <!-- 助手正文：Markdown 渲染 -->
              <div
                v-if="item.content && item.role === 'assistant'"
                class="md-body"
                v-html="renderMarkdown(item.content)"
              /><span v-if="item.streaming && item.role === 'assistant' && item.content" class="cursor">|</span>

              <!-- 用户消息：纯文本 -->
              <pre v-if="item.content && item.role === 'user'" class="user-text">{{ item.content }}</pre>

              <span v-else-if="item.streaming && !item.content" class="cursor standalone">|</span>
            </div>

            <!-- 消息操作栏（仅助手已完成消息） -->
            <div v-if="item.role === 'assistant' && !item.streaming" class="msg-actions">
              <el-tooltip content="复制内容" placement="top">
                <button class="msg-btn" @click="copyMessage(item.content)">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip
                v-if="index === messages.length - 1 && !streaming"
                content="重新生成"
                placement="top"
              >
                <button class="msg-btn" @click="regenerate">
                  <el-icon><RefreshRight /></el-icon>
                </button>
              </el-tooltip>
              <span class="msg-model-meta">
                <el-icon><Cpu /></el-icon>
                {{ item.modelName || (item.modelId ? item.modelId.split('/').pop() : '未知模型') }}
                <template v-if="item.tokens > 0"> · {{ item.tokens }} tokens</template>
              </span>
            </div>
          </div>
        </div>
      </div>

      <footer class="input-area">
        <div class="input-box-wrap" :class="{ 'is-focused': inputFocused, 'is-streaming': streaming }">
          <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 6 }"
            resize="none"
            placeholder="请输入你的问题，AI 将以中文与你对话…"
            @keydown.enter.exact.prevent="handleSend"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
        </div>
        <div class="input-actions">
          <span class="hint">Enter 发送 &nbsp;·&nbsp; Shift+Enter 换行</span>
          <div class="action-btns">
            <!-- 模型切换按钮 -->
            <el-popover
              v-model:visible="modelSwitchVisible"
              placement="top-end"
              :width="230"
              trigger="click"
              popper-class="model-switch-popover"
            >
              <template #reference>
                <button class="model-switch-btn" :title="config.activeModelId">
                  <el-icon><Cpu /></el-icon>
                  <span class="msb-name">{{ activeModelShortName }}</span>
                  <el-icon class="msb-arrow"><ArrowDown /></el-icon>
                </button>
              </template>
              <div class="msb-list">
                <div
                  v-for="m in config.models"
                  :key="m.id"
                  class="msb-item"
                  :class="{ active: m.id === config.activeModelId }"
                  @click="switchModel(m.id)"
                >
                  <span class="msb-item-name">{{ m.name || m.id }}</span>
                  <span class="msb-item-id">{{ m.id }}</span>
                </div>
                <div v-if="!config.models.length" class="msb-empty">暂无模型，请在设置中添加</div>
              </div>
            </el-popover>
            <el-button v-if="streaming" class="stop-btn" @click="stopStreaming">
              <el-icon><VideoPause /></el-icon>
              停止生成
            </el-button>
            <el-button v-else class="send-btn" :disabled="!inputText.trim()" @click="handleSend">
              <el-icon><Position /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </footer>
      <!-- 模型名称条 -->
      <div class="model-name-bar">
        <el-icon><MagicStick /></el-icon>
        <span>{{ config.activeModelId ? activeModelText : '未配置模型' }}</span>
        <span class="mnb-dot" :class="statusClass"></span>
      </div>
    </section>
    </div>

    <!-- 设置抽屉（全新设计）-->
    <el-drawer v-model="settingVisible" size="520px" :show-close="false" class="setting-drawer">
      <template #header>
        <div class="drawer-header">
          <div class="drawer-title">
            <div class="drawer-title-icon"><el-icon><Setting /></el-icon></div>
            <span>模型与API设置</span>
          </div>
          <el-button text circle @click="settingVisible = false"><el-icon><Close /></el-icon></el-button>
        </div>
      </template>

      <div class="setting-body">

        <!-- API 密钥 -->
        <div class="setting-section">
          <div class="section-header">
            <div class="section-label">API 密钥</div>
            <div class="section-tools">
              <span v-if="config.lastCheckedAt" class="check-time">最近检测：{{ config.lastCheckedAt }}</span>
            </div>
          </div>
          <div class="key-input-row">
            <el-input
              v-model="config.apiKey"
              placeholder="API 密钥"
              :type="apiKeyVisible ? 'text' : 'password'"
              class="key-input"
            >
              <template #suffix>
                <el-icon class="eye-icon" @click="apiKeyVisible = !apiKeyVisible">
                  <View v-if="!apiKeyVisible" /><Hide v-else />
                </el-icon>
              </template>
            </el-input>
            <el-button
              class="check-btn"
              :loading="checking"
              @click="checkAndSaveConfig"
            >检测</el-button>
          </div>
          <div class="section-hint-row">
            <span class="link-hint">前往 API 提供商获取密钥</span>
            <span class="right-hint">多个密钥使用逗号分隔</span>
          </div>
        </div>

        <!-- API 地址 -->
        <div class="setting-section">
          <div class="section-header">
            <div class="section-label">
              API 地址
              <el-tooltip content="支持填写根地址或完整 /v1/chat/completions 地址" placement="top">
                <el-icon class="help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </div>
          <el-input v-model="config.baseUrl" placeholder="例如 https://api.deepseek.com" clearable />
          <div v-if="completionUrlPreview" class="url-preview">
            <span class="preview-label">预览：</span>
            <span class="preview-url">{{ completionUrlPreview }}</span>
          </div>
        </div>

        <!-- 模型列表 -->
        <div class="setting-section">
          <div class="section-header">
            <div class="section-label">
              模型
              <span class="model-count">{{ config.models.length }}</span>
            </div>
          </div>

          <div class="model-groups">
            <div v-for="group in groupedModels" :key="group.name" class="model-group">
              <div class="group-title">
                <el-icon><ArrowDown /></el-icon>
                <span>{{ group.name }}</span>
              </div>
              <div
                v-for="model in group.models"
                :key="model.id"
                class="model-row"
                :class="{ 'is-active': model.id === config.activeModelId }"
                @click="config.activeModelId = model.id; saveConfig()"
              >
                <div class="model-row-avatar">
                  <el-icon><MagicStick /></el-icon>
                </div>
                <div class="model-row-info">
                  <div class="model-row-name">{{ model.name || model.id }}</div>
                  <div class="model-row-id">{{ model.id }}</div>
                </div>
                <div class="model-row-actions">
                  <el-radio
                    :model-value="config.activeModelId"
                    :value="model.id"
                    size="small"
                    @click.stop
                    @change="config.activeModelId = model.id; saveConfig()"
                  />
                  <el-icon
                    class="model-del-btn"
                    @click.stop="deleteModel(model.id)"
                  ><Delete /></el-icon>
                </div>
              </div>
            </div>

            <div v-if="!config.models.length" class="model-empty">
              暂无模型，请点击「添加」
            </div>
          </div>

          <div class="model-footer-btns">
            <el-button class="manage-btn" @click="addModelVisible = true">
              <el-icon><Plus /></el-icon>
              添加
            </el-button>
          </div>
        </div>

        <!-- 高级参数（折叠） -->
        <div class="setting-section">
          <div class="section-header advance-toggle" @click="advanceOpen = !advanceOpen">
            <div class="section-label">高级参数</div>
            <el-icon :class="['toggle-arrow', { open: advanceOpen }]"><ArrowDown /></el-icon>
          </div>
          <div v-if="advanceOpen" class="advance-body">
            <div class="adv-item">
              <div class="adv-label">
                创造性 <span class="adv-tag">Temperature</span>
                <span class="adv-val">{{ config.temperature }}</span>
              </div>
              <el-slider v-model="config.temperature" :min="0" :max="2" :step="0.1" />
              <div class="adv-desc">0 = 精确稳定 &nbsp;·&nbsp; 2 = 发散创意</div>
            </div>
            <div class="adv-item">
              <div class="adv-label">
                最大 Token
                <span class="adv-val">{{ config.maxTokens === 0 ? '不限制' : config.maxTokens }}</span>
              </div>
              <el-slider v-model="config.maxTokens" :min="0" :max="32000" :step="512" />
              <div class="adv-desc">0 = 不限制，建议 4096 ~ 16000</div>
            </div>
            <div class="adv-item">
              <div class="adv-label">
                上下文轮数
                <span class="adv-val">{{ config.contextRounds }} 轮</span>
              </div>
              <el-slider v-model="config.contextRounds" :min="1" :max="20" :step="1" />
              <div class="adv-desc">每轮 = 用户 + 助手各 1 条消息</div>
            </div>
          </div>
        </div>

      </div>
    </el-drawer>

    <el-dialog v-model="addModelVisible" title="添加模型" width="520px">
      <el-form label-position="top">
        <el-form-item label="模型 ID" required>
          <el-input v-model="newModel.id" placeholder="必填 例如 deepseek-ai/deepseek-r1" />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="newModel.name" placeholder="例如 DeepSeek R1" />
        </el-form-item>
        <el-form-item label="分组名称">
          <el-input v-model="newModel.group" placeholder="例如 Chat" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addModelVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddModel">添加模型</el-button>
      </template>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  CircleCheck,
  CircleClose,
  Close,
  CopyDocument,
  Cpu,
  Delete,
  Loading,
  MagicStick,
  Menu,
  Plus,
  Position,
  RefreshRight,
  Setting,
  UserFilled,
  VideoPause,
  View,
  Hide,
  QuestionFilled,
  ArrowDown
} from '@element-plus/icons-vue'
import { streamAiChat, testAiModel } from '@/api/aiAssistant'
import { marked } from 'marked'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'

// ── Markdown 配置 ─────────────────────────────────────────────
marked.use({
  renderer: (() => {
    const r = new marked.Renderer()
    r.code = ({ text, lang }) => {
      const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
      const highlighted = hljs.highlight(text, { language }).value
      return `<pre class="hljs-block"><code class="hljs language-${language}">${highlighted}</code></pre>`
    }
    return r
  })(),
  breaks: true,
  gfm: true
})

const renderMarkdown = (text) => {
  if (!text) return ''
  return DOMPurify.sanitize(marked.parse(text))
}

// ── 常量 ───────────────────────────────────────────────────────
const STORAGE_KEY  = 'ai_assistant_config_v1'
const SESSIONS_KEY = 'ai_assistant_sessions_v1'
const MAX_SESSIONS = 20

// ── 响应式数据 ─────────────────────────────────────────────────
const messagePanelRef  = ref(null)
const settingVisible   = ref(false)
const addModelVisible  = ref(false)
const checking         = ref(false)
const streaming        = ref(false)
const inputText        = ref('')
const messages         = ref([])
const abortControllerRef = ref(null)
const sidebarVisible   = ref(false)
const isMobile         = ref(window.innerWidth <= 1100)
const sessions         = ref([])
const currentSessionId = ref(null)
const inputFocused     = ref(false)
const apiKeyVisible    = ref(false)
const advanceOpen      = ref(false)
const statusHover      = ref(false)
const autoChecking     = ref(false)
const modelSwitchVisible = ref(false)

const config = reactive({
  apiKey: '',
  baseUrl: 'https://integrate.api.nvidia.com',
  models: [],
  activeModelId: '',
  tested: false,
  lastCheckedAt: '',
  temperature: 0.7,
  maxTokens: 0,
  contextRounds: 10
})

const newModel = reactive({ id: '', name: '', group: '' })

// ── Watch：配置变更自动重置 tested ─────────────────────────────
watch(
  () => [config.apiKey, config.baseUrl, config.activeModelId],
  () => {
    if (config.tested) {
      config.tested = false
      saveConfig()
    }
  }
)

// ── 屏幕尺寸监听 ───────────────────────────────────────────────
const silentCheck = async () => {
  if (!config.apiKey || !config.baseUrl || !config.activeModelId) return
  autoChecking.value = true
  try {
    const result = await testAiModel({ apiKey: config.apiKey, baseUrl: config.baseUrl, modelId: config.activeModelId })
    config.tested = !!result.ok
    if (result.ok) config.lastCheckedAt = new Date().toLocaleString('zh-CN')
    saveConfig()
  } catch { config.tested = false; saveConfig() }
  finally { autoChecking.value = false }
}

const onResize = () => { isMobile.value = window.innerWidth <= 1100 }
const cleanupViewState = () => {
  abortControllerRef.value?.abort()
  streaming.value = false
  sidebarVisible.value = false
  settingVisible.value = false
  addModelVisible.value = false
  modelSwitchVisible.value = false
  statusHover.value = false
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  loadConfig()
  loadSessions()
  silentCheck()
})
onUnmounted(() => {
  cleanupViewState()
  window.removeEventListener('resize', onResize)
})

// ── 计算属性 ───────────────────────────────────────────────────
const activeModelText = computed(() => {
  if (!config.activeModelId) return '未选择模型'
  const model = config.models.find(item => item.id === config.activeModelId)
  if (!model) return '未选择模型'
  return model.name ? `${model.name} / ${model.id}` : model.id
})

const currentSessionTitle = computed(() => {
  const s = sessions.value.find(s => s.id === currentSessionId.value)
  return s?.title || ''
})

const statusClass = computed(() => {
  if (autoChecking.value) return 'checking'
  if (!config.apiKey || !config.activeModelId) return 'idle'
  if (config.tested) return 'ok'
  return 'error'
})

const statusLabel = computed(() => {
  if (autoChecking.value) return '检测中...'
  if (!config.apiKey || !config.activeModelId) return '未配置'
  if (config.tested) return '已连接'
  return '未连通'
})

const activeModelShortName = computed(() => {
  if (!config.activeModelId) return '未选模型'
  const model = config.models.find(m => m.id === config.activeModelId)
  if (model?.name) return model.name
  return config.activeModelId.split('/').pop() || config.activeModelId
})

const groupedModels = computed(() => {
  const map = {}
  config.models.forEach(m => {
    const g = m.group || '默认'
    if (!map[g]) map[g] = []
    map[g].push(m)
  })
  return Object.entries(map).map(([name, models]) => ({ name, models }))
})

const completionUrlPreview = computed(() => {
  const base = (config.baseUrl || '').trim().replace(/\/+$/, '')
  if (!base) return ''
  if (base.includes('/chat/completions')) return base
  return `${base}/v1/chat/completions`
})

// ── 配置持久化 ─────────────────────────────────────────────────
const saveConfig = () => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
}

const loadConfig = () => {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (!saved) {
    config.models = [{ id: 'deepseek-ai/deepseek-r1', name: 'DeepSeek R1', group: '默认' }]
    config.activeModelId = config.models[0].id
    return
  }
  try {
    const parsed = JSON.parse(saved)
    config.apiKey         = parsed.apiKey         || ''
    config.baseUrl        = parsed.baseUrl        || 'https://integrate.api.nvidia.com'
    config.models         = Array.isArray(parsed.models) ? parsed.models : []
    config.activeModelId  = parsed.activeModelId  || ''
    config.tested         = !!parsed.tested
    config.lastCheckedAt  = parsed.lastCheckedAt  || ''
    config.temperature    = parsed.temperature    ?? 0.7
    config.maxTokens      = parsed.maxTokens      ?? 0
    config.contextRounds  = parsed.contextRounds  ?? 10
    if (!config.models.length) {
      config.models = [{ id: 'deepseek-ai/deepseek-r1', name: 'DeepSeek R1', group: '默认' }]
      config.activeModelId = config.models[0].id
    }
  } catch {
    config.models = [{ id: 'deepseek-ai/deepseek-r1', name: 'DeepSeek R1', group: '默认' }]
    config.activeModelId = config.models[0].id
  }
}

// ── 会话持久化 ─────────────────────────────────────────────────
const loadSessions = () => {
  try {
    const saved = localStorage.getItem(SESSIONS_KEY)
    if (saved) sessions.value = JSON.parse(saved)
  } catch {}
}

const saveSessions = () => {
  localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessions.value.slice(0, MAX_SESSIONS)))
}

const saveCurrentSession = () => {
  const completed = messages.value.filter(m => !m.streaming)
  if (!completed.length) return
  const title = completed.find(m => m.role === 'user')?.content?.slice(0, 20) || '新对话'
  if (currentSessionId.value) {
    const idx = sessions.value.findIndex(s => s.id === currentSessionId.value)
    if (idx >= 0) {
      sessions.value[idx].messages = completed
      sessions.value[idx].title = title
      saveSessions()
      return
    }
  }
  const id = Date.now()
  currentSessionId.value = id
  sessions.value.unshift({ id, title, messages: completed, createdAt: new Date().toLocaleString('zh-CN') })
  saveSessions()
}

const loadSession = (id) => {
  const s = sessions.value.find(s => s.id === id)
  if (!s) return
  abortControllerRef.value?.abort()
  streaming.value = false
  messages.value = s.messages.map(m => ({ ...m }))
  currentSessionId.value = id
  if (isMobile.value) sidebarVisible.value = false
  nextTick(() => scrollToBottom())
}

const deleteSession = (id) => {
  sessions.value = sessions.value.filter(s => s.id !== id)
  saveSessions()
  if (currentSessionId.value === id) {
    messages.value = []
    currentSessionId.value = null
  }
}

// ── 校验与错误映射 ─────────────────────────────────────────────
const validateConfig = () => {
  if (!config.apiKey.trim())        { ElMessage.warning('请先填写 API Key');  return false }
  if (!config.baseUrl.trim())       { ElMessage.warning('请先填写 API 地址'); return false }
  if (!config.activeModelId.trim()) { ElMessage.warning('请先选择模型');       return false }
  return true
}

const friendlyError = (error) => {
  const msg = error?.response?.data?.error?.message || error?.message || ''
  if (msg.includes('401') || /invalid.{0,10}api.{0,10}key|authentication/i.test(msg))
    return 'API Key 无效，请检查设置'
  if (msg.includes('429') || /rate.{0,10}limit/i.test(msg))
    return '请求频率超限，请稍后再试'
  if (msg.includes('402') || /insufficient|balance/i.test(msg))
    return '账户余额不足，请充值后使用'
  if (msg.includes('404') || /model.{0,20}not.{0,10}found/i.test(msg))
    return '模型 ID 不存在，请检查设置'
  if (msg.includes('500') || /server.{0,10}error/i.test(msg))
    return '服务器内部错误，请稍后重试'
  if (/timeout|network/i.test(msg))
    return '网络超时，请检查网络连接'
  return msg || '请求失败，请检查配置后重试'
}

// ── 设置操作 ───────────────────────────────────────────────────
const openSettings = () => { settingVisible.value = true }

const checkAndSaveConfig = async () => {
  if (!validateConfig()) return
  checking.value = true
  try {
    const result = await testAiModel({
      apiKey: config.apiKey,
      baseUrl: config.baseUrl,
      modelId: config.activeModelId
    })
    if (result.ok) {
      config.tested = true
      config.lastCheckedAt = new Date().toLocaleString('zh-CN')
      saveConfig()
      ElMessage.success('检测通过，配置已保存')
      return
    }
    config.tested = false
    saveConfig()
    ElMessage.error('检测失败：未返回有效内容')
  } catch (error) {
    config.tested = false
    saveConfig()
    ElMessage.error(friendlyError(error))
  } finally {
    checking.value = false
  }
}

const confirmAddModel = () => {
  const modelId = newModel.id.trim()
  if (!modelId) { ElMessage.warning('模型 ID 为必填项'); return }
  if (config.models.some(item => item.id === modelId)) { ElMessage.warning('该模型 ID 已存在'); return }
  config.models.push({ id: modelId, name: newModel.name.trim(), group: newModel.group.trim() })
  if (!config.activeModelId) config.activeModelId = modelId
  newModel.id = ''; newModel.name = ''; newModel.group = ''
  addModelVisible.value = false
  config.tested = false
  saveConfig()
  ElMessage.success('模型添加成功')
}

const deleteCurrentModel = () => {
  if (!config.activeModelId) return
  const index = config.models.findIndex(item => item.id === config.activeModelId)
  if (index < 0) return
  config.models.splice(index, 1)
  config.activeModelId = config.models[0]?.id || ''
  config.tested = false
  saveConfig()
  ElMessage.success('模型已删除')
}

const deleteModel = (id) => {
  const index = config.models.findIndex(item => item.id === id)
  if (index < 0) return
  config.models.splice(index, 1)
  if (config.activeModelId === id) {
    config.activeModelId = config.models[0]?.id || ''
  }
  config.tested = false
  saveConfig()
  ElMessage.success('模型已删除')
}

const switchModel = (id) => {
  if (config.activeModelId === id) { modelSwitchVisible.value = false; return }
  config.activeModelId = id
  config.tested = false
  modelSwitchVisible.value = false
  saveConfig()
  silentCheck()
}

// ── 会话控制 ───────────────────────────────────────────────────
const startNewConversation = () => {
  saveCurrentSession()
  abortControllerRef.value?.abort()
  streaming.value = false
  messages.value = []
  inputText.value = ''
  currentSessionId.value = null
}

const scrollToBottom = async () => {
  await nextTick()
  if (!messagePanelRef.value) return
  messagePanelRef.value.scrollTop = messagePanelRef.value.scrollHeight
}

const buildRequestMessages = () => {
  const maxMsg = (config.contextRounds || 10) * 2
  const completed = messages.value
    .filter(item => !item.streaming && item.content)
    .slice(-maxMsg)
  return [
    { role: 'system', content: '你是图书管理系统中的AI智能助手，名字叫"智馆AI"。请始终使用中文交流、使用中文思考。对于复杂问题请先展示思考过程再给出答案。' },
    ...completed.map(item => ({ role: item.role, content: item.content }))
  ]
}

// ── 消息操作 ───────────────────────────────────────────────────
const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const stopStreaming = () => { abortControllerRef.value?.abort() }

// ── 核心发送逻辑（复用于发送和重新生成） ────────────────────────
const sendMessage = async (requestMessages) => {
  const assistantId = Date.now() + 1
  const usedModelId = config.activeModelId
  const usedModelName = config.models.find(m => m.id === usedModelId)?.name || ''
  messages.value.push({
    id: assistantId, role: 'assistant',
    content: '', thinking: '', streaming: true, stopped: false,
    modelId: usedModelId, modelName: usedModelName, tokens: 0
  })
  await scrollToBottom()

  const controller = new AbortController()
  abortControllerRef.value = controller
  streaming.value = true

  const getAssistant = () => messages.value.find(m => m.id === assistantId)

  try {
    await streamAiChat({
      apiKey:    config.apiKey,
      baseUrl:   config.baseUrl,
      modelId:   config.activeModelId,
      messages:  requestMessages,
      signal:    controller.signal,
      temperature: config.temperature,
      maxTokens: config.maxTokens,
      onChunk: async ({ type, text }) => {
        const msg = getAssistant()
        if (!msg) return
        if (type === 'thinking') msg.thinking += text
        else msg.content += text
        await scrollToBottom()
      },
      onDone: ({ usage } = {}) => {
        const msg = getAssistant()
        if (msg) {
          msg.streaming = false
          if (usage?.total_tokens) msg.tokens = usage.total_tokens
        }
      }
    })
  } catch (error) {
    const msg = getAssistant()
    if (msg) {
      msg.streaming = false
      if (error?.name === 'AbortError') {
        msg.stopped = true
      } else {
        ElMessage.error(friendlyError(error))
      }
    }
  } finally {
    const msg = getAssistant()
    if (msg) {
      msg.streaming = false
      if (!msg.content && !msg.thinking && !msg.stopped) {
        msg.content = '未返回内容，请检查配置后重试。'
      }
    }
    streaming.value = false
    abortControllerRef.value = null
    saveCurrentSession()
  }
}

const handleSend = async () => {
  const content = inputText.value.trim()
  if (!content || streaming.value) return
  if (!validateConfig()) return
  if (!config.tested) { ElMessage.warning('请先在设置中检测配置可用性'); return }

  messages.value.push({ id: Date.now(), role: 'user', content })
  inputText.value = ''
  await scrollToBottom()
  const requestMessages = buildRequestMessages()
  await sendMessage(requestMessages)
}

const regenerate = async () => {
  if (streaming.value) return
  const lastIdx = messages.value.length - 1
  if (messages.value[lastIdx]?.role !== 'assistant') return
  messages.value.splice(lastIdx, 1)
  const requestMessages = buildRequestMessages()
  await sendMessage(requestMessages)
}
</script>

<style scoped>
@import 'highlight.js/styles/github.css';

/* ============================================================
   全局关键帧动画
   ============================================================ */

/* 彩色渐变流动（用于边框 & 背景） */
@keyframes rainbow-flow {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 空状态图标漂浮 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-10px); }
}

/* 流式气泡发光脉冲 */
@keyframes stream-glow {
  0%, 100% {
    box-shadow: 0 0 10px rgba(56, 189, 248, 0.35),
                inset 0 0 0 1.5px rgba(56, 189, 248, 0.35);
  }
  50% {
    box-shadow: 0 0 20px rgba(139, 92, 246, 0.4),
                inset 0 0 0 1.5px rgba(139, 92, 246, 0.4);
  }
}

/* 消息淡入上浮 */
@keyframes fade-up {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* AI头像光晕脉冲 */
@keyframes avatar-glow {
  0%, 100% { box-shadow: 0 0 8px rgba(56, 189, 248, 0.5); }
  50%       { box-shadow: 0 0 18px rgba(139, 92, 246, 0.65); }
}

/* 渐变背景动画（用于品牌头像 & 移动按钮） */
@keyframes brand-shimmer {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 侧边栏边框呼吸光 */
@keyframes sidebar-glow {
  0%, 100% {
    box-shadow: 0 10px 30px rgba(14, 165, 233, 0.12),
                0 0 0 1px rgba(56, 189, 248, 0.2);
  }
  50% {
    box-shadow: 0 10px 30px rgba(139, 92, 246, 0.15),
                0 0 0 1px rgba(139, 92, 246, 0.28);
  }
}

/* 打字机光标闪烁 */
@keyframes blink {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0; }
}

/* 思考徽章呼吸 */
@keyframes thinking-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.55; }
}

/* ============================================================
   页面整体布局
   ============================================================ */

.ai-page {
  height: calc(100vh - 72px);
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  overflow: hidden;
  position: relative;
  padding: 16px 20px;
  box-sizing: border-box;
  /* 极淡紫蓝渐变背景，带动画 */
  background: linear-gradient(145deg, #f0f9ff 0%, #faf5ff 50%, #f0f9ff 100%);
  background-size: 200% 200%;
  animation: rainbow-flow 10s ease infinite;
}

/* ── 移动端遮罩 ── */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.28);
  z-index: 99;
  backdrop-filter: blur(3px);
}

/* ── 移动端切换按钮 ── */
.mobile-toggle {
  display: none;
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 200;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  background-size: 200% 200%;
  animation: brand-shimmer 3s ease infinite;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.5);
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.mobile-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(139, 92, 246, 0.65);
}

/* ============================================================
   侧边栏
   ============================================================ */

.ai-sidebar {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(56, 189, 248, 0.18);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  overflow: hidden;        /* 不整个边栏滚动，只让会话列表内部滚动 */
  box-sizing: border-box;
  animation: sidebar-glow 4s ease-in-out infinite;
  transition: border-color 0.3s, box-shadow 0.3s;
}
.ai-sidebar:hover {
  animation: none;
  border-color: rgba(139, 92, 246, 0.35);
  box-shadow: 0 12px 40px rgba(139, 92, 246, 0.15);
}
/* 自定义滚动条 */
.ai-sidebar::-webkit-scrollbar { width: 4px; }
.ai-sidebar::-webkit-scrollbar-track { background: transparent; }
.ai-sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(#38bdf8, #8b5cf6);
  border-radius: 4px;
}

/* 品牌区域 */
.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-avatar {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #38bdf8 0%, #8b5cf6 50%, #ec4899 100%);
  background-size: 200% 200%;
  animation: brand-shimmer 3s ease infinite;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.45);
}
.brand-block h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  background: linear-gradient(90deg, #0c4a6e, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.brand-block p {
  margin: 3px 0 0;
  color: #64748b;
  font-size: 12px;
}

.full-btn { width: 100%; transition: transform 0.15s; }
.full-btn:active { transform: scale(0.98); }

/* 状态卡片 */
.status-card {
  border-radius: 14px;
  padding: 12px;
  border: 1px solid transparent;
  transition: transform 0.2s, box-shadow 0.2s;
}
.status-card:hover { transform: translateY(-1px); }
.status-card.ok {
  background: rgba(16, 185, 129, 0.07);
  border-color: rgba(16, 185, 129, 0.25);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.1);
}
.status-card.warn {
  background: rgba(245, 158, 11, 0.07);
  border-color: rgba(245, 158, 11, 0.25);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.1);
}
.status-title { font-size: 13px; color: #475569; margin-bottom: 4px; }
.status-content { display: flex; flex-direction: column; gap: 2px; font-weight: 600; color: #0f172a; }
.status-content small { color: #64748b; font-weight: 500; }

/* ── 历史会话 ── */
.session-list-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;            /* ★ 占满剩余空间 */
  min-height: 0;      /* ★ flex 必须 */
  overflow: hidden;
}
.session-list-title {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  flex-shrink: 0;
}
.session-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;            /* ★ 占满剩余空间 */
  overflow-y: auto;
  min-height: 0;
  /* 移除原 max-height: 220px 限制 */
}
.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #334155;
  font-size: 13px;
  border: 1px solid transparent;
}
.session-item:hover {
  background: rgba(14, 165, 233, 0.08);
  border-color: rgba(56, 189, 248, 0.2);
  transform: translateX(2px);
}
.session-item.active {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.12), rgba(139, 92, 246, 0.1));
  color: #0284c7;
  font-weight: 600;
  border-color: rgba(56, 189, 248, 0.3);
}
.session-icon { flex-shrink: 0; color: #94a3b8; }
.session-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-del {
  flex-shrink: 0;
  color: #cbd5e1;
  font-size: 12px;
  border-radius: 4px;
  padding: 2px;
  transition: all 0.15s;
}
.session-del:hover { color: #ef4444; transform: scale(1.2); }

/* 提示区域 */
.tips-block {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.06), rgba(139, 92, 246, 0.06));
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(56, 189, 248, 0.12);
  flex-shrink: 0;   /* ★ 不被压缩，始终显示在底部 */
}
.tips-block h4 { margin: 0 0 8px; color: #0c4a6e; font-size: 13px; }
.tips-block ul {
  margin: 0;
  padding-left: 18px;
  color: #334155;
  line-height: 1.75;
  font-size: 12px;
}

/* ============================================================
   主聊天区 ── 彩色流动边框外壳
   ============================================================ */

.ai-main-wrap {
  border-radius: 20px;
  padding: 2px;
  /* 流动彩色渐变边框 */
  background: linear-gradient(
    135deg,
    #38bdf8 0%,
    #818cf8 20%,
    #8b5cf6 40%,
    #ec4899 60%,
    #f59e0b 80%,
    #38bdf8 100%
  );
  background-size: 300% 300%;
  animation: rainbow-flow 5s ease infinite;
  height: 100%;
  min-height: 0;          /* ★ 关键：修复 grid 子项高度溢出 bug */
  overflow: hidden;       /* ★ 关键：锁住内容不溢出 */
  box-sizing: border-box;
}

/* ── 主聊天内容 ── */
.ai-main {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;          /* ★ 关键：flex 子项也需要 */
  overflow: hidden;
  box-sizing: border-box;
}

/* 顶部标题栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  flex-shrink: 0;
  background: linear-gradient(
    135deg,
    rgba(240, 249, 255, 0.85) 0%,
    rgba(250, 245, 255, 0.85) 100%
  );
}
.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 16px;
  background: linear-gradient(90deg, #0c4a6e, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.chat-actions { display: flex; align-items: center; gap: 6px; }

/* ── 消息列表 ── */
.message-panel {
  flex: 1;
  overflow-y: auto;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 0;
}
.message-panel::-webkit-scrollbar { width: 5px; }
.message-panel::-webkit-scrollbar-track { background: transparent; }
.message-panel::-webkit-scrollbar-thumb {
  background: linear-gradient(#38bdf8, #8b5cf6);
  border-radius: 4px;
}

/* 空状态 */
.empty-state {
  margin: auto;
  text-align: center;
  color: #64748b;
  padding: 40px 20px;
}
.empty-state .el-icon {
  color: #8b5cf6;
  animation: float 3.5s ease-in-out infinite;
  display: block;
  margin: 0 auto 16px;
  filter: drop-shadow(0 4px 12px rgba(139, 92, 246, 0.35));
}
.empty-state h3 {
  margin: 0 0 8px;
  background: linear-gradient(90deg, #0c4a6e, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 20px;
  font-weight: 700;
}
.empty-state p { font-size: 14px; line-height: 1.6; }

/* ── 消息行 ── */
.message-row {
  display: flex;
  gap: 12px;
  animation: fade-up 0.3s ease both;
}
.message-row.user { flex-direction: row-reverse; }

/* 头像 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  margin-top: 2px;
  transition: transform 0.2s;
}
.avatar:hover { transform: scale(1.1); }
.avatar.assistant {
  background: linear-gradient(135deg, #38bdf8 0%, #8b5cf6 60%, #ec4899 100%);
  background-size: 200% 200%;
  animation: brand-shimmer 4s ease infinite, avatar-glow 3s ease-in-out infinite;
  box-shadow: 0 2px 10px rgba(139, 92, 246, 0.4);
}
.avatar.user {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.35);
}

/* 气泡容器 */
.bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: min(85%, 860px);
}
.message-row.user .bubble-wrap { align-items: flex-end; }

/* ── 气泡 ── */
.bubble {
  padding: 12px 16px;
  border-radius: 16px;
  background: #f8fafc;
  color: #0f172a;
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s, transform 0.15s;
}
.bubble:hover {
  box-shadow: 0 4px 18px rgba(14, 165, 233, 0.12);
}

/* 流式输出气泡 */
.bubble.streaming {
  border: none;
  background: linear-gradient(
    135deg,
    rgba(240, 249, 255, 0.92),
    rgba(245, 243, 255, 0.92)
  );
  animation: stream-glow 1.6s ease-in-out infinite;
}

/* 已停止气泡 */
.bubble.stopped {
  border: 1px solid rgba(245, 158, 11, 0.3);
  background: rgba(255, 251, 235, 0.8);
}

/* 用户气泡 */
.message-row.user .bubble {
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  border: none;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.4);
}
.message-row.user .bubble:hover {
  box-shadow: 0 6px 24px rgba(14, 165, 233, 0.5);
  transform: translateY(-1px);
}

/* 已停止标记 */
.stopped-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #b45309;
  background: rgba(251, 191, 36, 0.12);
  border-radius: 6px;
  padding: 3px 8px;
  margin-bottom: 8px;
  width: fit-content;
}

/* ── 消息操作按钮 ── */
.msg-actions {
  display: flex;
  gap: 4px;
  align-items: center;
}
.msg-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.85);
  border-radius: 8px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  backdrop-filter: blur(4px);
}
.msg-btn:hover {
  background: linear-gradient(
    135deg,
    rgba(56, 189, 248, 0.15),
    rgba(139, 92, 246, 0.15)
  );
  border-color: rgba(56, 189, 248, 0.4);
  color: #0ea5e9;
  transform: scale(1.12);
}

/* ── 用户消息文本 ── */
.user-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  line-height: 1.6;
}

/* ── Markdown 渲染 ── */
.md-body { line-height: 1.75; color: inherit; word-break: break-word; }

.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3),
.md-body :deep(h4) { margin: 14px 0 7px; font-weight: 700; color: #0f172a; }
.md-body :deep(h1) { font-size: 1.3em; }
.md-body :deep(h2) { font-size: 1.15em; }
.md-body :deep(h3) { font-size: 1.05em; }
.md-body :deep(p) { margin: 6px 0; }
.md-body :deep(ul),
.md-body :deep(ol) { margin: 6px 0; padding-left: 22px; }
.md-body :deep(li) { margin: 3px 0; }

.md-body :deep(code) {
  background: rgba(14, 165, 233, 0.1);
  color: #0369a1;
  padding: 2px 6px;
  border-radius: 5px;
  font-size: 0.87em;
  font-family: 'Consolas', 'Fira Code', monospace;
}
.md-body :deep(.hljs-block) {
  margin: 10px 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
.md-body :deep(.hljs-block code) {
  display: block;
  padding: 14px 16px;
  background: #f8fafc;
  color: inherit;
  font-size: 0.87em;
  line-height: 1.6;
  overflow-x: auto;
  border-radius: 0;
}
.md-body :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 14px;
  border-left: 3px solid #8b5cf6;
  background: linear-gradient(
    135deg,
    rgba(56, 189, 248, 0.06),
    rgba(139, 92, 246, 0.06)
  );
  border-radius: 0 10px 10px 0;
  color: #475569;
}
.md-body :deep(table) { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 0.9em; }
.md-body :deep(th),
.md-body :deep(td) { padding: 7px 12px; border: 1px solid #e2e8f0; text-align: left; }
.md-body :deep(th) {
  background: linear-gradient(135deg, #f0f9ff, #faf5ff);
  font-weight: 600;
}
.md-body :deep(a) { color: #0ea5e9; text-decoration: underline; }
.md-body :deep(hr) { border: none; border-top: 1px solid #e2e8f0; margin: 12px 0; }

/* ── 打字机光标 ── */
.cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: linear-gradient(#38bdf8, #8b5cf6);
  animation: blink 0.8s step-end infinite;
  vertical-align: text-bottom;
  margin-left: 1px;
  border-radius: 1px;
}
.cursor.standalone {
  display: block;
  width: 10px;
  height: 18px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  border-radius: 2px;
  animation: blink 0.8s step-end infinite;
}

/* ── 思考块 ── */
.thinking-block {
  margin-bottom: 10px;
  background: linear-gradient(
    135deg,
    rgba(139, 92, 246, 0.05),
    rgba(196, 181, 253, 0.08)
  );
  border: 1px solid rgba(139, 92, 246, 0.22);
  border-radius: 12px;
  overflow: hidden;
}
.thinking-summary {
  cursor: pointer;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #7c3aed;
  font-weight: 600;
  user-select: none;
  list-style: none;
  transition: background 0.2s;
}
.thinking-summary:hover { background: rgba(139, 92, 246, 0.07); }
.thinking-summary::-webkit-details-marker { display: none; }
.thinking-badge {
  font-size: 12px;
  font-weight: 500;
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.12);
  padding: 1px 8px;
  border-radius: 10px;
  animation: thinking-pulse 1.5s ease infinite;
}
.thinking-content {
  padding: 8px 14px 12px;
  border-top: 1px solid rgba(139, 92, 246, 0.12);
  color: #4c1d95;
  font-size: 13px;
}

/* ── 输入区域 ── */
.input-area {
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  padding: 14px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;           /* ★ 不压缩，始终固定在底部 */
  background: linear-gradient(
    180deg,
    rgba(248, 250, 252, 0.7) 0%,
    rgba(245, 243, 255, 0.85) 100%
  );
  backdrop-filter: blur(12px);
  transition: background 0.3s;
}

/* ── 输入框包装容器（带流动彩色边框） ── */
.input-box-wrap {
  position: relative;
  border-radius: 16px;
  padding: 2px;
  background: linear-gradient(
    135deg,
    rgba(148, 163, 184, 0.35) 0%,
    rgba(148, 163, 184, 0.25) 100%
  );
  transition: background 0.3s, box-shadow 0.3s;
}

/* 聚焦时边框切换为彩色流动 */
.input-box-wrap.is-focused {
  background: linear-gradient(
    135deg,
    #38bdf8 0%,
    #818cf8 25%,
    #8b5cf6 50%,
    #ec4899 75%,
    #38bdf8 100%
  );
  background-size: 300% 300%;
  animation: rainbow-flow 3s ease infinite;
  box-shadow:
    0 0 0 3px rgba(56, 189, 248, 0.12),
    0 4px 20px rgba(139, 92, 246, 0.2);
}

/* 流式输出时红色脉冲边框 */
.input-box-wrap.is-streaming {
  background: linear-gradient(135deg, #f43f5e, #fb923c, #f43f5e);
  background-size: 200% 200%;
  animation: rainbow-flow 2s ease infinite;
  box-shadow: 0 0 0 3px rgba(244, 63, 94, 0.12);
}

/* 内层 textarea 重置边框，让外层渐变边框显现 */
.input-box-wrap :deep(.el-textarea__inner) {
  border-radius: 14px;
  border: none;
  background: rgba(255, 255, 255, 0.96);
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.65;
  color: #0f172a;
  resize: none;
  box-shadow: none;
  transition: background 0.25s;
}
.input-box-wrap :deep(.el-textarea__inner:focus) {
  outline: none;
  box-shadow: none;
  background: #ffffff;
}
.input-box-wrap :deep(.el-textarea__inner::placeholder) {
  color: #94a3b8;
  font-size: 13.5px;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hint { color: #94a3b8; font-size: 12px; letter-spacing: 0.2px; }
.action-btns { display: flex; gap: 8px; align-items: center; }

/* 发送按钮 */
.send-btn {
  border: none !important;
  border-radius: 12px !important;
  padding: 8px 20px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6) !important;
  background-size: 200% 200% !important;
  color: #fff !important;
  transition: all 0.25s !important;
  animation: brand-shimmer 4s ease infinite !important;
  box-shadow: 0 3px 12px rgba(139, 92, 246, 0.35) !important;
}
.send-btn:hover:not(:disabled) {
  transform: translateY(-1px) scale(1.04) !important;
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5) !important;
}
.send-btn:active:not(:disabled) {
  transform: translateY(0) scale(0.98) !important;
}
.send-btn:disabled {
  opacity: 0.45 !important;
  animation: none !important;
  background: linear-gradient(135deg, #94a3b8, #cbd5e1) !important;
  box-shadow: none !important;
}

/* 停止按钮 */
.stop-btn {
  border: 1.5px solid rgba(244, 63, 94, 0.45) !important;
  border-radius: 12px !important;
  padding: 8px 20px !important;
  font-weight: 600 !important;
  background: rgba(255, 241, 242, 0.9) !important;
  color: #e11d48 !important;
  transition: all 0.2s !important;
  animation: thinking-pulse 1.8s ease-in-out infinite !important;
}
.stop-btn:hover {
  background: rgba(244, 63, 94, 0.12) !important;
  border-color: rgba(244, 63, 94, 0.7) !important;
  transform: scale(1.04) !important;
}

/* ── 设置抽屉全新样式 ── */

/* 覆写 el-drawer 内部的 header 区域 */
:global(.setting-drawer .el-drawer__header) {
  padding: 18px 24px 14px !important;
  border-bottom: 1px solid rgba(226, 232, 240, 0.7) !important;
  margin-bottom: 0 !important;
}
:global(.setting-drawer .el-drawer__body) {
  padding: 0 24px !important;
  overflow-y: auto !important;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.drawer-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
.drawer-title-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}

/* 设置内容区 */
.setting-body {
  padding: 0 0 32px;
  display: flex;
  flex-direction: column;
}
.setting-section {
  padding: 20px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.7);
}
.setting-section:last-child { border-bottom: none; }

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-label {
  font-weight: 700;
  font-size: 14px;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-tools { display: flex; align-items: center; gap: 8px; }
.check-time { font-size: 12px; color: #10b981; font-weight: 500; }
.help-icon { color: #94a3b8; cursor: help; font-size: 14px; }

/* API Key 行 */
.key-input-row { display: flex; gap: 8px; align-items: center; }
.key-input { flex: 1; }
.eye-icon { cursor: pointer; color: #94a3b8; transition: color .2s; }
.eye-icon:hover { color: #0ea5e9; }
.check-btn {
  border: none !important;
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6) !important;
  background-size: 200% 200% !important;
  animation: brand-shimmer 4s ease infinite !important;
  color: #fff !important;
  border-radius: 10px !important;
  padding: 0 20px !important;
  height: 36px !important;
  font-weight: 600 !important;
  white-space: nowrap;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(14, 165, 233, 0.3) !important;
  transition: transform .2s, box-shadow .2s !important;
}
.check-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.45) !important;
}
.section-hint-row {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
}
.link-hint { color: #0ea5e9; cursor: pointer; }
.link-hint:hover { text-decoration: underline; }
.right-hint { color: #94a3b8; }

/* URL 预览 */
.url-preview {
  margin-top: 10px;
  padding: 9px 13px;
  background: rgba(14, 165, 233, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(56, 189, 248, 0.18);
}
.preview-label { font-size: 12px; color: #94a3b8; margin-right: 4px; }
.preview-url {
  font-size: 12px;
  color: #0369a1;
  font-family: 'Consolas', 'Fira Code', monospace;
  word-break: break-all;
}

/* 模型数量徽章 */
.model-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  border-radius: 10px;
  margin-left: 4px;
}

/* 模型分组列表 */
.model-groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}
.model-group {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba(248,250,252,1), rgba(245,243,255,0.6));
  font-size: 13px;
  font-weight: 700;
  color: #374151;
  border-bottom: 1px solid rgba(226, 232, 240, 0.7);
}
.group-title .el-icon { color: #8b5cf6; font-size: 12px; }

/* 单个模型行 */
.model-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background .2s;
  border-bottom: 1px solid rgba(226,232,240,0.4);
}
.model-row:last-child { border-bottom: none; }
.model-row:hover { background: rgba(56, 189, 248, 0.05); }
.model-row.is-active {
  background: linear-gradient(135deg, rgba(56,189,248,0.08), rgba(139,92,246,0.06));
}
.model-row-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  background-size: 200% 200%;
  animation: brand-shimmer 5s ease infinite;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
}
.model-row-info { flex: 1; min-width: 0; }
.model-row-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.model-row-id {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.model-row-actions { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.model-del-btn {
  color: #cbd5e1;
  font-size: 15px;
  cursor: pointer;
  transition: all .2s;
  padding: 4px;
  border-radius: 6px;
}
.model-del-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.08);
  transform: scale(1.15);
}
.model-empty {
  padding: 28px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

/* 模型底部按钮 */
.model-footer-btns { display: flex; gap: 10px; }
.manage-btn {
  border-radius: 10px !important;
  font-weight: 600 !important;
  border: 1px solid rgba(56, 189, 248, 0.4) !important;
  color: #0ea5e9 !important;
  background: rgba(56, 189, 248, 0.06) !important;
  transition: all .2s !important;
}
.manage-btn:hover {
  background: rgba(56, 189, 248, 0.14) !important;
  border-color: rgba(56, 189, 248, 0.65) !important;
  transform: translateY(-1px) !important;
}

/* 高级参数 */
.advance-toggle { cursor: pointer; user-select: none; }
.toggle-arrow { transition: transform .28s; color: #94a3b8; }
.toggle-arrow.open { transform: rotate(180deg); }
.advance-body {
  padding-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.adv-item { display: flex; flex-direction: column; gap: 8px; }
.adv-label {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  gap: 6px;
}
.adv-tag {
  font-size: 11px;
  background: rgba(139, 92, 246, 0.1);
  color: #7c3aed;
  padding: 2px 7px;
  border-radius: 6px;
  font-weight: 500;
}
.adv-val {
  margin-left: auto;
  font-size: 13px;
  font-weight: 700;
  color: #0ea5e9;
  min-width: 60px;
  text-align: right;
}
.adv-desc { font-size: 12px; color: #94a3b8; }

/* ── 响应式 ── */
@media (max-width: 1100px) {
  .ai-page {
    grid-template-columns: 1fr;
    height: calc(100vh - 72px);
    padding: 8px;
  }
  .mobile-toggle { display: flex; }
  .ai-main-wrap { height: 100%; }
  .ai-sidebar {
    position: fixed;
    top: 72px;
    left: 0;
    bottom: 0;
    width: 280px;
    z-index: 100;
    border-radius: 0 18px 18px 0;
    transform: translateX(-100%);
    transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
    animation: none;
  }
  .ai-sidebar.mobile-visible {
    transform: translateX(0);
    box-shadow: 4px 0 32px rgba(139, 92, 246, 0.2);
  }
}

/* ── 新建对话按钮动画 ─────────────────────────────────────────── */
.new-conv-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 10px 0;
  margin-bottom: 8px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c3aed 0%, #a855f7 50%, #6366f1 100%);
  background-size: 200% 200%;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  animation: conv-btn-bg 4s ease infinite;
}
.new-conv-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 24px rgba(139, 92, 246, 0.5);
}
.new-conv-btn:active {
  transform: scale(0.97);
}
.new-conv-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  z-index: 1;
}
.new-conv-icon {
  font-size: 16px;
  transition: transform 0.3s;
}
.new-conv-btn:hover .new-conv-icon {
  transform: rotate(90deg);
}
.new-conv-shimmer {
  position: absolute;
  top: -50%;
  left: -60%;
  width: 40%;
  height: 200%;
  background: linear-gradient(105deg, transparent 30%, rgba(255,255,255,0.35) 50%, transparent 70%);
  transform: skewX(-20deg);
  animation: shimmer-slide 3s ease infinite;
}
@keyframes conv-btn-bg {
  0%, 100% { background-position: 0% 50%; }
  50%       { background-position: 100% 50%; }
}
@keyframes shimmer-slide {
  0%   { left: -60%; }
  60%  { left: 120%; }
  100% { left: 120%; }
}

/* ── 设置+状态合并行 ─────────────────────────────────────────── */
.settings-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  color: #c4b5fd;
  font-size: 13px;
  font-weight: 500;
}
.settings-status-row:hover {
  background: rgba(139, 92, 246, 0.22);
  color: #e9d5ff;
}
.ssr-left {
  display: flex;
  align-items: center;
  gap: 7px;
  color: inherit;
}
.ssr-left .el-icon {
  font-size: 15px;
  color: #a78bfa;
  flex-shrink: 0;
}
.ssr-left span {
  color: inherit;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.2px;
}
.ssr-right {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* ── 状态指示点 ──────────────────────────────────────────────── */
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: background 0.3s;
}
.status-dot.ok   { background: #22c55e; box-shadow: 0 0 6px rgba(34,197,94,0.7); }
.status-dot.error { background: #ef4444; box-shadow: 0 0 6px rgba(239,68,68,0.7); }
.status-dot.idle  { background: #94a3b8; }
.status-dot.checking {
  background: #f59e0b;
  animation: dot-pulse 1s ease infinite;
}
@keyframes dot-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.5; transform: scale(0.7); }
}

.status-dot-label {
  font-size: 11px;
  font-weight: 600;
}
.status-dot-label.ok      { color: #22c55e; }
.status-dot-label.error   { color: #ef4444; }
.status-dot-label.idle    { color: #94a3b8; }
.status-dot-label.checking { color: #f59e0b; }

/* ── 状态展开卡片（悬浮显示） ────────────────────────────────── */
.status-expand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 4px;
  border-radius: 10px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  font-size: 12px;
  overflow: hidden;
  transform-origin: top;
}
.se-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.se-badge.ok      { background: rgba(34,197,94,0.15);  color: #22c55e; }
.se-badge.error   { background: rgba(239,68,68,0.15);  color: #ef4444; }
.se-badge.idle    { background: rgba(148,163,184,0.15); color: #94a3b8; }
.se-badge.checking { background: rgba(245,158,11,0.15); color: #f59e0b; }
.se-model {
  color: #c4b5fd;
  font-size: 11px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.spin { animation: icon-spin 1s linear infinite; }
@keyframes icon-spin { to { transform: rotate(360deg); } }

/* slide-down transition */
.slide-down-enter-active { transition: all 0.22s ease; }
.slide-down-leave-active { transition: all 0.18s ease; }
.slide-down-enter-from  { opacity: 0; transform: scaleY(0.6) translateY(-8px); }
.slide-down-leave-to    { opacity: 0; transform: scaleY(0.6) translateY(-8px); }

/* fade-text transition */
.fade-text-enter-active { transition: opacity 0.2s; }
.fade-text-leave-active { transition: opacity 0.15s; }
.fade-text-enter-from, .fade-text-leave-to { opacity: 0; }

/* ── 历史会话空状态 / 数量角标 ───────────────────────────────── */
.session-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: rgba(139, 92, 246, 0.5);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}
.session-empty {
  text-align: center;
  padding: 16px 0;
  color: rgba(255,255,255,0.3);
  font-size: 12px;
}

/* ── 模型名称条（对话框底部） ────────────────────────────────── */
.model-name-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 5px 0;
  font-size: 11px;
  color: rgba(255,255,255,0.35);
  border-top: 1px solid rgba(255,255,255,0.06);
  background: rgba(15, 12, 30, 0.4);
  flex-shrink: 0;
}
.model-name-bar .el-icon { font-size: 12px; }
.mnb-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.mnb-dot.ok      { background: #22c55e; }
.mnb-dot.error   { background: #ef4444; }
.mnb-dot.idle    { background: #94a3b8; }
.mnb-dot.checking { background: #f59e0b; animation: dot-pulse 1s ease infinite; }

/* ── 消息底部模型+Token元信息 ─────────────────────────────────── */
.msg-model-meta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 6px;
  padding: 2px 9px;
  border-radius: 10px;
  background: rgba(139, 92, 246, 0.07);
  border: 1px solid rgba(139, 92, 246, 0.18);
  color: #7c3aed;
  font-size: 11px;
  vertical-align: middle;
  letter-spacing: 0.1px;
  white-space: nowrap;
  overflow: hidden;
  max-width: 220px;
  text-overflow: ellipsis;
}
.msg-model-meta .el-icon { font-size: 11px; flex-shrink: 0; color: #8b5cf6; }

/* ── 输入区模型切换按钮 ───────────────────────────────────────── */
.model-switch-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  border: 1px solid rgba(139, 92, 246, 0.35);
  border-radius: 8px;
  background: rgba(139, 92, 246, 0.07);
  color: #6d28d9;
  font-size: 12px;
  cursor: pointer;
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  transition: all 0.2s;
  flex-shrink: 0;
}
.model-switch-btn:hover {
  background: rgba(139, 92, 246, 0.15);
  color: #5b21b6;
  border-color: rgba(109, 40, 217, 0.55);
}
.model-switch-btn .el-icon:first-child { font-size: 13px; flex-shrink: 0; }
.msb-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msb-arrow { font-size: 10px; opacity: 0.7; flex-shrink: 0; }

/* ── 模型切换弹出列表（通过 popper-class 控制全局） ─────────────── */
.msb-list { display: flex; flex-direction: column; gap: 2px; max-height: 260px; overflow-y: auto; }
.msb-item {
  display: flex;
  flex-direction: column;
  padding: 8px 10px;
  border-radius: 7px;
  cursor: pointer;
  transition: background 0.15s;
}
.msb-item:hover { background: rgba(139, 92, 246, 0.1); }
.msb-item.active {
  background: rgba(139, 92, 246, 0.18);
  border-left: 3px solid #8b5cf6;
  padding-left: 7px;
}
.msb-item-name { font-size: 13px; font-weight: 500; color: #1f2937; }
.msb-item-id {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msb-empty { padding: 14px; text-align: center; color: #9ca3af; font-size: 12px; }
</style>
