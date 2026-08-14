<template>
  <el-drawer
    :model-value="visible"
    direction="rtl"
    :size="460"
    :with-header="false"
    class="ai-drawer"
    @close="$emit('close')"
  >
    <div class="ai-shell">
      <!-- 头部 -->
      <div class="ai-head">
        <div class="ai-brand">
          <span class="ai-orb"></span>
          <div>
            <p class="ai-title">AI 助手</p>
            <p class="ai-sub">社团全流程管理系统 · mimo-v2.5</p>
          </div>
        </div>
        <el-button text circle class="ai-close" @click="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 主体：会话侧轨 + 对话区 -->
      <div class="ai-body">
        <div class="ai-sessions">
          <button class="ai-new" title="新对话" @click="newChat">
            <el-icon><Plus /></el-icon>
          </button>
          <div v-for="s in sessions" :key="s.id" class="ai-session-item"
               :class="{ active: s.id === activeSessionId }"
               :title="s.title" @click="openSession(s.id)">
            {{ s.title.slice(0, 2) }}
          </div>
        </div>

        <div class="ai-chat">
          <div ref="scrollRef" class="ai-messages">
            <div v-if="messages.length === 0" class="ai-empty">
              <p class="ai-empty-icon">✦</p>
              <p class="ai-empty-title">问点什么</p>
              <p class="ai-empty-hint">我是本系统的智能助手，接下来将逐步接入运维分析、数据问答与社团知识库能力</p>
            </div>
            <div v-for="(m, i) in messages" :key="i" class="ai-msg" :class="m.role">
              <div v-if="m.role === 'assistant'" class="ai-avatar">AI</div>
              <div class="ai-bubble">
                <template v-if="m.role === 'assistant' && m.streaming && m.content === ''">
                  <span class="ai-cursor"></span>
                </template>
                <template v-else>{{ m.content }}</template>
                <span v-if="m.role === 'assistant' && m.streaming" class="ai-cursor-inline"></span>
              </div>
            </div>
          </div>

          <div class="ai-input-bar">
            <textarea
              v-model="input"
              class="ai-input"
              rows="2"
              placeholder="输入你的问题…（Enter 发送，Shift+Enter 换行）"
              @keydown.enter.exact.prevent="send"
            ></textarea>
            <button class="ai-send" :disabled="streaming || !input.trim()" @click="send">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { Close, Plus, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { streamChat, listSessions, listMessages, type AgentSession } from '../api/agent'

const props = defineProps<{ visible: boolean }>()
defineEmits<{ (e: 'close'): void }>()

interface Msg {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

const sessions = ref<AgentSession[]>([])
const activeSessionId = ref<number | null>(null)
const messages = ref<Msg[]>([])
const input = ref('')
const streaming = ref(false)
const scrollRef = ref<HTMLElement>()

watch(
  () => props.visible,
  (v) => {
    if (v && sessions.value.length === 0) loadSessions()
  },
)

async function loadSessions() {
  try {
    const { data } = await listSessions()
    sessions.value = data
  } catch {
    /* 会话列表失败不阻断对话 */
  }
}

function newChat() {
  activeSessionId.value = null
  messages.value = []
  input.value = ''
}

async function openSession(id: number) {
  activeSessionId.value = id
  try {
    const { data } = await listMessages(id)
    messages.value = data
      .filter((m) => m.content)
      .map((m) => ({ role: m.role, content: m.content }))
    scrollToBottom()
  } catch {
    ElMessage.error('加载会话失败')
  }
}

async function send() {
  const content = input.value.trim()
  if (!content || streaming.value) return
  input.value = ''
  messages.value.push({ role: 'user', content })
  const assistant: Msg = { role: 'assistant', content: '', streaming: true }
  messages.value.push(assistant)
  streaming.value = true
  scrollToBottom()

  await streamChat(
    activeSessionId.value,
    content,
    (chunk) => {
      assistant.content += chunk
      scrollToBottom()
    },
    (meta) => {
      assistant.streaming = false
      activeSessionId.value = meta.sessionId
      loadSessions()
    },
    (msg) => {
      assistant.streaming = false
      if (!assistant.content) assistant.content = `⚠ ${msg}`
      ElMessage.error(msg)
    },
  )
  streaming.value = false
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    scrollRef.value?.scrollTo({ top: scrollRef.value.scrollHeight, behavior: 'smooth' })
  })
}
</script>

<style scoped>
.ai-drawer :deep(.el-drawer__body) {
  padding: 0;
  background: #12141d;
}

.ai-shell {
  height: 100%;
  display: flex;
  flex-direction: column;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ---------- 头部 ---------- */
.ai-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.ai-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-orb {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: radial-gradient(circle at 32% 30%, #6ee7b7, #10b981 55%, #065f46);
  box-shadow: 0 0 18px rgba(52, 211, 153, 0.45);
  position: relative;
}

.ai-orb::after {
  content: '';
  position: absolute;
  inset: 5px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.55);
}

.ai-title {
  margin: 0;
  color: #f1f5f9;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.ai-sub {
  margin: 2px 0 0;
  color: #64748b;
  font-size: 11px;
}

.ai-close {
  color: #64748b;
}

.ai-close:hover {
  color: #f1f5f9;
  background: rgba(255, 255, 255, 0.06);
}

/* ---------- 主体 ---------- */
.ai-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

/* 会话侧轨 */
.ai-sessions {
  width: 56px;
  flex-shrink: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.ai-new {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1.5px dashed rgba(52, 211, 153, 0.5);
  background: rgba(52, 211, 153, 0.08);
  color: #34d399;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-new:hover {
  background: rgba(52, 211, 153, 0.2);
  border-style: solid;
}

.ai-session-item {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  color: #94a3b8;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.ai-session-item:hover {
  background: rgba(255, 255, 255, 0.09);
  color: #e2e8f0;
}

.ai-session-item.active {
  background: rgba(52, 211, 153, 0.16);
  color: #34d399;
  box-shadow: inset 0 0 0 1px rgba(52, 211, 153, 0.4);
}

/* 对话区 */
.ai-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-empty {
  margin: auto;
  text-align: center;
  color: #475569;
  padding: 0 24px;
}

.ai-empty-icon {
  font-size: 28px;
  color: #34d399;
  opacity: 0.7;
  margin: 0 0 10px;
}

.ai-empty-title {
  margin: 0 0 8px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 600;
}

.ai-empty-hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
}

.ai-msg {
  display: flex;
  gap: 8px;
  animation: rise 0.25s ease;
}

.ai-msg.user {
  justify-content: flex-end;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.ai-avatar {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border-radius: 9px;
  background: linear-gradient(135deg, #10b981, #065f46);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}

.ai-bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}

.ai-msg.assistant .ai-bubble {
  background: rgba(255, 255, 255, 0.055);
  color: #dbe3ee;
  border-top-left-radius: 4px;
}

.ai-msg.user .ai-bubble {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #1c1206;
  font-weight: 500;
  border-top-right-radius: 4px;
}

.ai-cursor {
  display: inline-block;
  width: 8px;
  height: 14px;
  background: #34d399;
  border-radius: 2px;
  animation: blink 0.8s infinite;
}

.ai-cursor-inline {
  display: inline-block;
  width: 6px;
  height: 12px;
  background: #34d399;
  border-radius: 2px;
  margin-left: 2px;
  vertical-align: -2px;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.15;
  }
}

/* 输入区 */
.ai-input-bar {
  padding: 12px 14px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.ai-input {
  flex: 1;
  resize: none;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 14px;
  padding: 10px 14px;
  color: #e2e8f0;
  font-size: 13px;
  font-family: inherit;
  line-height: 1.6;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.ai-input:focus {
  border-color: rgba(52, 211, 153, 0.6);
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.12);
}

.ai-input::placeholder {
  color: #475569;
}

.ai-send {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #34d399, #059669);
  color: #04231a;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-send:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(52, 211, 153, 0.35);
}

.ai-send:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
</style>
