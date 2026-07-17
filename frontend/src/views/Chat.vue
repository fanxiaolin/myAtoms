<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const chatInput = ref('')
const messages = ref([])
const isLoading = ref(false)
const activeTab = ref('discover')
const thinkingSteps = ref([])

const showMenu = ref(false)
const menuPosition = ref({ left: '0px', bottom: '0px' })
const menuBtnRef = ref(null)
const teamMode = ref(false)
const deepResearch1 = ref(false)

const navItems = [
  { name: 'discover', label: '发现' },
  { name: 'templates', label: '模板' },
  { name: 'profile', label: '我的' }
]

const API_URL = '/api/chat/completions'

const scrollToBottom = () => {
  setTimeout(() => {
    const container = document.querySelector('.chat-container')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 100)
}

const toggleMenu = (e) => {
  e.stopPropagation()
  showMenu.value = !showMenu.value
  if (showMenu.value && menuBtnRef.value) {
    const rect = menuBtnRef.value.getBoundingClientRect()
    menuPosition.value = {
      left: rect.left + 'px',
      bottom: 'calc(100vh - ' + rect.top + 'px + 8px)'
    }
  }
}

const closeMenu = (e) => {
  if (showMenu.value) {
    showMenu.value = false
  }
}

const handleFileUpload = (type) => {
  const input = document.createElement('input')
  input.type = 'file'
  
  if (type === 'folder') {
    input.webkitdirectory = true
    input.multiple = true
  } else {
    input.multiple = true
  }
  
  input.addEventListener('change', (e) => {
    const files = Array.from(e.target.files)
    if (files.length > 0) {
      const fileNames = files.map(f => f.name).join(', ')
      showToast(`已选择 ${files.length} 个文件：${fileNames}`)
    }
    e.target.value = ''
  })
  
  input.click()
}

const showToast = (message) => {
  const toast = document.createElement('div')
  toast.style.cssText = `
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0,0,0,0.8);
    color: white;
    padding: 12px 24px;
    border-radius: 8px;
    font-size: 14px;
    z-index: 9999;
  `
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => toast.remove(), 2000)
}

const simulateThinking = async () => {
  thinkingSteps.value = []
  const steps = [
    'I\'m getting started.',
    'This is a casual chat question asking for travel recommendations. I\'ll respond directly with helpful suggestions.',
    '...'
  ]
  
  for (let i = 0; i < steps.length; i++) {
    await new Promise(resolve => setTimeout(resolve, 800))
    thinkingSteps.value.push(steps[i])
    scrollToBottom()
  }
}

const callChatAPI = async (userMessage) => {
  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        messages: [
          { role: 'system', content: '你是一个有用的助手x' },
          ...messages.value,
          { role: 'user', content: userMessage }
        ]
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    
    if (data.choices && data.choices.length > 0) {
      return data.choices[0].message.content
    }
    
    return '抱歉，我无法理解您的请求。'
  } catch (error) {
    console.error('Chat API error:', error)
    return '抱歉，服务暂时不可用，请稍后再试。'
  }
}

const handleEnter = (e) => {
  if (e.ctrlKey || e.metaKey) {
    return
  }
  e.preventDefault()
  sendMessage()
}

const sendMessage = async () => {
  if (!chatInput.value.trim()) return
  
  const userContent = chatInput.value.trim()
  
  messages.value.push({
    role: 'user',
    content: userContent,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  chatInput.value = ''
  scrollToBottom()
  isLoading.value = true
  
  await simulateThinking()
  
  messages.value.push({
    role: 'thinking',
    steps: [...thinkingSteps.value],
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  thinkingSteps.value = []
  
  const responseContent = await callChatAPI(userContent)
  
  messages.value.push({
    role: 'assistant',
    content: responseContent,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  isLoading.value = false
  scrollToBottom()
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'discover') {
    router.push('/')
  } else if (tab === 'templates') {
    router.push('/')
  } else if (tab === 'profile') {
    router.push('/')
  }
}

onMounted(async () => {
  document.addEventListener('click', closeMenu)
  
  const initialMessage = route.query.message || '请Alex构建一个Web应用。'
  
  messages.value.push({
    role: 'user',
    content: initialMessage,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  scrollToBottom()
  isLoading.value = true
  
  await simulateThinking()
  
  messages.value.push({
    role: 'thinking',
    steps: [...thinkingSteps.value],
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  thinkingSteps.value = []
  
  const responseContent = await callChatAPI(initialMessage)
  
  messages.value.push({
    role: 'assistant',
    content: responseContent,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  isLoading.value = false
  scrollToBottom()
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<template>
  <div class="dashboard">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <span class="logo-icon">⚛</span>
          <span class="logo-text">Atoms</span>
        </div>
        <nav class="nav">
          <button 
            v-for="item in navItems" 
            :key="item.name"
            :class="['nav-item', { active: activeTab === item.name }]"
            @click="switchTab(item.name)"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>
    </header>

    <main class="main">
      <div class="chat-container">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message', message.role]"
        >
          <template v-if="message.role === 'thinking'">
            <div class="thinking-avatar">🤖</div>
            <div class="thinking-content">
              <div class="thinking-header">
                <span class="thinking-name">Alex</span>
                <span class="thinking-tag">工程师</span>
              </div>
              <div class="thinking-steps">
                <div class="steps-summary">
                  <span class="check-icon">✓</span>
                  <span>已处理 {{ message.steps.length }} 步</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
                <div class="steps-list">
                  <div v-for="(step, idx) in message.steps" :key="idx" class="step-item">
                    <span class="step-dot"></span>
                    <span class="step-text">{{ step }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="message-avatar">
              <span v-if="message.role === 'user'">👤</span>
              <span v-else>🤖</span>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-name">{{ message.role === 'user' ? '我' : 'Alex' }}</span>
                <span v-if="message.role === 'assistant'" class="processing-tag">工程师</span>
                <span class="message-time">{{ message.time }}</span>
              </div>
              <div class="message-text">{{ message.content }}</div>
            </div>
          </template>
        </div>
        
        <div v-if="isLoading" class="thinking-message">
          <div class="thinking-avatar">🤖</div>
          <div class="thinking-content">
            <div class="thinking-header">
              <span class="thinking-name">Alex</span>
              <span class="thinking-tag">工程师</span>
            </div>
            <div class="thinking-steps">
              <div class="steps-summary">
                <span class="check-icon">✓</span>
                <span>已处理 {{ thinkingSteps.length }} 步</span>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </div>
              <div class="steps-list">
                <div v-for="(step, idx) in thinkingSteps" :key="idx" class="step-item">
                  <span class="step-dot"></span>
                  <span class="step-text">{{ step }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <textarea 
          v-model="chatInput" 
          class="chat-input" 
          placeholder="让智能团队实现你的想法"
          rows="2"
          @keydown.enter="handleEnter"
        ></textarea>
        <div class="input-actions">
          <button ref="menuBtnRef" class="action-btn menu-toggle-btn" @click="toggleMenu">
            <svg v-if="!showMenu" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          
          <div v-if="showMenu" class="dropdown-menu" :style="menuPosition" @click.stop>
            <div class="menu-item" @click="teamMode = !teamMode">
              <span class="menu-icon">👥</span>
              <span class="menu-text">团队模式</span>
              <span :class="['menu-toggle', { off: !teamMode }]"></span>
            </div>
            <div class="menu-item has-submenu" @click.stop>
              <span class="menu-icon">📎</span>
              <span class="menu-text">附件</span>
              <span class="menu-arrow">›</span>
              
              <div class="submenu">
                <div class="submenu-item" @click="handleFileUpload('file')">
                  <span class="submenu-icon">📄</span>
                  <span class="submenu-text">上传文件</span>
                </div>
                <div class="submenu-item" @click="handleFileUpload('folder')">
                  <span class="submenu-icon">📁</span>
                  <span class="submenu-text">上传文件夹</span>
                </div>
              </div>
            </div>
            <div class="menu-item has-submenu" @click="showToast('连接器功能开发中')">
              <span class="menu-icon">🔗</span>
              <span class="menu-text">连接器</span>
              <span class="menu-arrow">›</span>
            </div>
            <div class="menu-item" @click="showToast('视频功能开发中')">
              <span class="menu-icon">🎬</span>
              <span class="menu-text">视频</span>
              <span class="menu-badge">Seedance 2.0</span>
              <span class="menu-dot"></span>
            </div>
            <div class="menu-item" @click="deepResearch1 = !deepResearch1">
              <span class="menu-icon">🔍</span>
              <span class="menu-text">深度研究</span>
              <span :class="['menu-toggle', { off: !deepResearch1 }]"></span>
            </div>
            <div class="menu-item has-submenu" @click="showToast('竞赛模式开发中')">
              <span class="menu-icon">🏆</span>
              <span class="menu-text">竞赛模式</span>
              <span class="menu-arrow">›</span>
            </div>
          </div>

          <button class="send-btn" @click="sendMessage">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  background: #f5f5f5;
  width: 100%;
  min-height: 100vh;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eee;
}

.header-content {
  width: 100%;
  padding: 1vw 3vw;
  display: flex;
  align-items: center;
  gap: 4vw;
  box-sizing: border-box;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5vw;
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  cursor: pointer;
}

.logo-icon {
  font-size: 1.4rem;
}

.logo-text {
  color: #333;
}

.nav {
  display: flex;
  gap: 2vw;
}

.nav-item {
  padding: 0.5vw 1vw;
  font-size: clamp(0.85rem, 1.2vw, 1rem);
  color: #666;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  color: #333;
}

.nav-item.active {
  color: #3b82f6;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #3b82f6;
  border-radius: 2px;
}

.main {
  width: 100%;
  padding: 100px 3vw 0;
  box-sizing: border-box;
  background: #f5f5f5;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 1vw;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 100%;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #3b82f6;
}

.message-content {
  max-width: calc(100% - 52px);
  width: fit-content;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message.user .message-header {
  flex-direction: row-reverse;
}

.message-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.processing-tag {
  font-size: 11px;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  max-width: 100%;
}

.message.user .message-text {
  background: #3b82f6;
  color: #fff;
  border-radius: 12px 12px 0 12px;
}

.thinking-message {
  display: flex;
  gap: 12px;
}

.thinking-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.thinking-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.thinking-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.thinking-tag {
  font-size: 11px;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.thinking-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.steps-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #3b82f6;
}

.check-icon {
  color: #10b981;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  margin-top: 6px;
  flex-shrink: 0;
}

.step-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.chat-input-area {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border: 1px solid #e5e5e5;
  flex-shrink: 0;
}

.chat-input {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  color: #333;
  background: transparent;
  margin-bottom: 12px;
  line-height: 1.6;
  min-height: 48px;
}

.chat-input::placeholder {
  color: #999;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #e8e8e8;
}

.send-btn {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.dropdown-menu {
  position: fixed;
  min-width: 200px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid #e5e5e5;
  padding: 8px 0;
  z-index: 1000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.menu-text {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.menu-arrow {
  font-size: 16px;
  color: #999;
}

.menu-toggle {
  width: 36px;
  height: 20px;
  background: #667eea;
  border-radius: 10px;
  position: relative;
}

.menu-toggle::after {
  content: '';
  position: absolute;
  top: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  transition: all 0.2s;
}

.menu-toggle.off {
  background: #ddd;
}

.menu-toggle.off::after {
  right: auto;
  left: 2px;
}

.menu-badge {
  font-size: 12px;
  padding: 2px 6px;
  background: #f0f0f0;
  border-radius: 4px;
  color: #667eea;
}

.menu-dot {
  width: 6px;
  height: 6px;
  background: #ff6b6b;
  border-radius: 50%;
}

.has-submenu .submenu {
  position: absolute;
  left: 100%;
  top: 0;
  min-width: 160px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid #e5e5e5;
  padding: 8px 0;
  display: none;
}

.has-submenu:hover .submenu {
  display: block;
}

.submenu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.submenu-item:hover {
  background: #f8f9fa;
}

.submenu-icon {
  font-size: 14px;
}

.submenu-text {
  font-size: 14px;
  color: #333;
}
</style>
