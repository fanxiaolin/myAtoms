<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import SiteHeader from '../components/SiteHeader.vue'
import { addPendingFiles, pendingFiles, removePendingFile } from '../stores/attachmentStore'

marked.setOptions({
  breaks: true,
  gfm: true
})

const route = useRoute()
const router = useRouter()

const chatInput = ref('')
const messages = ref([])
const isLoading = ref(false)
const activeRequestController = ref(null)
const activeTab = ref('discover')
const thinkingSteps = ref([])
const isThinking = ref(false)

const generatedCode = ref('')
const previewContent = ref('')
const isPreviewLoading = ref(false)
const rightPanelRef = ref(null)
const previewHeightPercent = ref(35)
const isResizingPreview = ref(false)
let resizeHandle = null
let resizePointerId = null
let resizeAnimationFrame = null
let pendingResizePercent = null

const resizePreview = (event) => {
  if (!isResizingPreview.value || !rightPanelRef.value) return

  const rect = rightPanelRef.value.getBoundingClientRect()
  const nextPercent = ((event.clientY - rect.top) / rect.height) * 100
  const clampedPercent = Math.min(75, Math.max(20, nextPercent))
  pendingResizePercent = clampedPercent

  if (resizeAnimationFrame) cancelAnimationFrame(resizeAnimationFrame)
  resizeAnimationFrame = requestAnimationFrame(() => {
    previewHeightPercent.value = clampedPercent
    resizeAnimationFrame = null
  })
}

const stopPreviewResize = () => {
  if (!isResizingPreview.value) return
  isResizingPreview.value = false
  if (resizeAnimationFrame) {
    cancelAnimationFrame(resizeAnimationFrame)
    resizeAnimationFrame = null
  }
  if (pendingResizePercent !== null) {
    previewHeightPercent.value = pendingResizePercent
    pendingResizePercent = null
  }
  if (resizeHandle && resizePointerId !== null && resizeHandle.hasPointerCapture(resizePointerId)) {
    resizeHandle.releasePointerCapture(resizePointerId)
  }
  resizeHandle = null
  resizePointerId = null
  document.body.style.removeProperty('cursor')
  document.body.style.removeProperty('user-select')
  window.removeEventListener('pointermove', resizePreview)
  window.removeEventListener('pointerup', stopPreviewResize)
  window.removeEventListener('pointercancel', stopPreviewResize)
}

const startPreviewResize = (event) => {
  event.preventDefault()
  resizeHandle = event.currentTarget
  resizePointerId = event.pointerId
  resizeHandle.setPointerCapture(resizePointerId)
  isResizingPreview.value = true
  document.body.style.cursor = 'row-resize'
  document.body.style.userSelect = 'none'
  window.addEventListener('pointermove', resizePreview)
  window.addEventListener('pointerup', stopPreviewResize)
  window.addEventListener('pointercancel', stopPreviewResize)
}

const copyCode = async () => {
  if (generatedCode.value) {
    await navigator.clipboard.writeText(generatedCode.value)
    showToast('代码已复制')
  }
}

const decodeHTML = (html) => {
  const textarea = document.createElement('textarea')
  textarea.innerHTML = html
  return textarea.value
}

const escapeHTML = (str) => {
  return str.replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[char]))
}

const renderMessage = (content) => {
  const escaped = escapeHTML(content)
  return escaped
    .replace(/```(?:\w+)?\s*\n?([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/\[([^\]]+)\]\((https?:\/\/[^)]+)\)/g, '<a href="$2" target="_blank" rel="noreferrer">$1 ↗</a>')
    .replace(/(?<!["'=])(https?:\/\/[^\s<]+)/g, '<a href="$1" target="_blank" rel="noreferrer">$1 ↗</a>')
}

const showMenu = ref(false)
const menuPosition = ref({ left: '0px', bottom: '0px' })
const menuBtnRef = ref(null)
const teamMode = ref(false)
const deepResearch1 = ref(false)

const MAX_ATTACHMENT_FILE_BYTES = 512 * 1024
const MAX_ATTACHMENT_TOTAL_CHARS = 240000
const TEXT_FILE_EXTENSIONS = new Set([
  'txt', 'md', 'markdown', 'json', 'jsonl', 'csv', 'tsv', 'xml', 'yaml', 'yml',
  'html', 'htm', 'css', 'scss', 'less', 'js', 'jsx', 'ts', 'tsx', 'vue', 'svelte',
  'java', 'kt', 'kts', 'py', 'rb', 'php', 'go', 'rs', 'c', 'h', 'cpp', 'hpp',
  'cs', 'swift', 'sql', 'sh', 'bash', 'zsh', 'fish', 'ps1', 'properties', 'toml',
  'ini', 'conf', 'env', 'gitignore', 'dockerfile', 'gradle', 'graphql', 'gql'
])

const navItems = [
  { name: 'discover', label: '发现' },
  { name: 'templates', label: '模板' },
  { name: 'profile', label: '我的' }
]

const API_URL = '/api/chat/completions'

const scrollToBottom = () => {
  const container = document.querySelector('.chat-container')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
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
      const { ignored } = addPendingFiles(files)
      showMenu.value = false
      showToast(`已加载 ${pendingFiles.value.length} 个附件${ignored ? `，另有 ${ignored} 个超出数量限制` : ''}`)
    }
    e.target.value = ''
  })
  
  input.click()
}

const isTextAttachment = (file) => {
  if (file.type.startsWith('text/')) return true
  const baseName = file.name.toLowerCase()
  const extension = baseName.includes('.') ? baseName.split('.').pop() : baseName
  return TEXT_FILE_EXTENSIONS.has(extension)
}

const prepareAttachments = async (files, signal) => {
  const attachments = []
  const skipped = []
  let totalChars = 0

  for (const item of files) {
    if (signal.aborted) return { attachments: [], skipped: [] }
    if (!isTextAttachment(item.file)) {
      skipped.push(`${item.path}（暂不支持该格式）`)
      continue
    }
    if (item.file.size > MAX_ATTACHMENT_FILE_BYTES) {
      skipped.push(`${item.path}（超过 512KB）`)
      continue
    }
    let content
    try {
      content = await item.file.text()
    } catch (_) {
      skipped.push(`${item.path}（读取失败）`)
      continue
    }
    if (totalChars + content.length > MAX_ATTACHMENT_TOTAL_CHARS) {
      skipped.push(`${item.path}（附件总内容过大）`)
      continue
    }
    totalChars += content.length
    attachments.push({ name: item.file.name, path: item.path, type: item.file.type, content })
  }

  return { attachments, skipped }
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

const simulateThinking = async (signal) => {
  thinkingSteps.value = []
  isThinking.value = true
  const mode = route.query.mode || 'engineer'
  const steps = mode === 'deep_research'
    ? ['正在拆解研究问题', '正在检索并交叉验证多个来源', '正在组织结论、争议与引用']
    : mode === 'team'
      ? ['正在分析需求与任务范围', '正在选择协作 Agent 与 Skill', '正在生成可执行方案']
      : ['正在理解任务', '正在规划实现步骤', '正在生成结果']
  
  for (let i = 0; i < steps.length; i++) {
    await new Promise(resolve => setTimeout(resolve, 800))
    if (signal?.aborted) return false
    thinkingSteps.value.push(steps[i])
    scrollToBottom()
  }
  return true
}

const callChatAPI = async (attachments, controller) => {
  // 超时按“无数据空闲时间”计算，而不是限制整次生成总时长。
  // 长 HTML 只要仍在持续流式输出就不会被误杀。
  let timeout = null
  const resetIdleTimeout = () => {
    if (timeout) window.clearTimeout(timeout)
    timeout = window.setTimeout(() => controller.abort(), 180000)
  }
  resetIdleTimeout()
  let fullContent = ''
  try {
    const STREAM_URL = '/api/chat/completions/stream'
    const response = await fetch(STREAM_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        messages: [
          { role: 'system', content: '你是一个有用的助手x' },
          ...messages.value
            .filter(msg => msg.role !== 'thinking')
            .map(msg => ({ role: msg.role, content: msg.content }))
        ],
        stream: true,
        mode: route.query.mode || (deepResearch1.value ? 'deep_research' : teamMode.value ? 'team' : 'engineer'),
        skillIds: route.query.skill ? [route.query.skill] : [],
        knowledgeIds: [],
        attachments
      }),
      signal: controller.signal
    })

    if (!response.ok) {
      const raw = await response.text()
      let detail = raw
      try {
        const payload = JSON.parse(raw)
        detail = payload.detail || payload.message || payload.error?.message || raw
      } catch (_) { /* 非 JSON 错误体直接展示 */ }
      if (response.status === 503 && !detail) detail = '后端未配置 SiliconFlow API Key，或模型服务暂时不可用'
      throw new Error(`${detail || '请求失败'}（HTTP ${response.status}）`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let displayContent = ''
    let lastScrollTime = 0
    let assistantIndex = null
    let sseBuffer = ''
    let streamFinished = false

    const applyDelta = (content) => {
      isThinking.value = false
      if (assistantIndex === null) {
        assistantIndex = messages.value.push({
          role: 'assistant',
          content: '',
          time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        }) - 1
      }

      fullContent += content
      // 始终根据完整内容重新渲染，避免 ``` 或 HTML 标签被拆在两个网络分片中。
      displayContent = renderMessage(fullContent)
      messages.value[assistantIndex].content = displayContent

      const streamingCode = extractStreamingHTML(fullContent)
      if (streamingCode) {
        isPreviewLoading.value = true
        generatedCode.value = streamingCode
      }

      const now = Date.now()
      if (now - lastScrollTime > 100) {
        scrollToBottom()
        lastScrollTime = now
      }
    }

    const processSSEEvent = (event) => {
      const dataStr = event
        .split('\n')
        .filter(line => line.startsWith('data:'))
        .map(line => line.slice(5).replace(/^ /, ''))
        .join('\n')

      if (!dataStr) return false
      if (dataStr.trim() === '[DONE]') return true

      try {
        const data = JSON.parse(dataStr)
        const choice = data.choices?.[0]
        const content = choice?.delta?.content
        if (typeof content === 'string' && content.length > 0) {
          applyDelta(content)
        }
        return choice?.finish_reason === 'stop'
      } catch (error) {
        // 此处拿到的一定是由空行终止的完整 SSE 事件；解析失败才是真正的服务端数据问题。
        console.warn('Invalid SSE event:', dataStr, error)
        return false
      }
    }

    while (!streamFinished) {
      const { done, value } = await reader.read()
      if (value?.length) resetIdleTimeout()
      sseBuffer += decoder.decode(value || new Uint8Array(), { stream: !done })
      // 同时兼容服务端的 CRLF 和 LF；未结束的事件继续留在缓冲区等待下次 read()。
      sseBuffer = sseBuffer.replace(/\r\n/g, '\n')

      let eventEnd = sseBuffer.indexOf('\n\n')
      while (eventEnd !== -1) {
        const event = sseBuffer.slice(0, eventEnd)
        sseBuffer = sseBuffer.slice(eventEnd + 2)
        streamFinished = processSSEEvent(event)
        if (streamFinished) break
        eventEnd = sseBuffer.indexOf('\n\n')
      }

      if (done) {
        if (!streamFinished && sseBuffer.trim()) {
          streamFinished = processSSEEvent(sseBuffer.trim())
        }
        break
      }
    }

    reader.releaseLock()
    isPreviewLoading.value = false
    processCodeAndPreview(fullContent)
    
    return fullContent
  } catch (error) {
    console.error('Chat API error:', error)
    isPreviewLoading.value = false
    isThinking.value = false
    if (error?.name === 'AbortError' && controller.userAborted) {
      if (fullContent) processCodeAndPreview(fullContent)
      return fullContent
    }
    const errorText = error?.name === 'AbortError'
      ? '请求超时：模型连续 3 分钟没有返回新数据。请检查 SiliconFlow 服务状态和后端日志后重试。'
      : `请求失败：${error?.message || '服务暂时不可用'}`
    messages.value.push({ role: 'assistant', content: errorText,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
    return errorText
  } finally {
    if (timeout) window.clearTimeout(timeout)
    if (activeRequestController.value === controller) activeRequestController.value = null
    isThinking.value = false
    isLoading.value = false
  }
}

const extractStreamingHTML = (content) => {
  const fencedStart = content.match(/```(?:html)?\s*\n?/i)
  if (fencedStart?.index !== undefined) {
    const start = fencedStart.index + fencedStart[0].length
    const remainder = content.slice(start)
    const closingFence = remainder.indexOf('```')
    return closingFence === -1 ? remainder : remainder.slice(0, closingFence)
  }

  const htmlStart = content.search(/<!doctype\s+html|<html\b/i)
  return htmlStart === -1 ? '' : content.slice(htmlStart)
}

const extractHTMLCode = (code) => {
  const htmlMatch = code.match(/<!DOCTYPE[\s\S]*<\/html>/) || code.match(/<html[\s\S]*<\/html>/)
  if (htmlMatch) {
    return htmlMatch[0]
  }
  
  const bodyMatch = code.match(/<body([^>]*)>([\s\S]*?)<\/body>/gi)
  if (bodyMatch && bodyMatch.length > 0) {
    return bodyMatch[bodyMatch.length - 1]
  }
  
  return code
}

const processCodeAndPreview = (content) => {
  if (generatedCode.value) {
    const htmlCode = extractHTMLCode(generatedCode.value)
    generatedCode.value = htmlCode
    
    let previewHTML = htmlCode.trim()
    
    if (!previewHTML.startsWith('<!DOCTYPE') && !previewHTML.startsWith('<html')) {
      const bodyMatch = previewHTML.match(/<body([^>]*)>([\s\S]*?)<\/body>/i)
      if (bodyMatch) {
        previewHTML = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head><body${bodyMatch[1]}>${bodyMatch[2]}</body></html>`
      } else {
        previewHTML = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head><body>${previewHTML}</body></html>`
      }
    }
    
    previewContent.value = previewHTML
    return
  }
  
  const codeBlockMatch = content.match(/```html\s*([\s\S]*?)\s*```/) || content.match(/```\s*([\s\S]*?)\s*```/)
  
  if (codeBlockMatch) {
    const rawCode = codeBlockMatch[1]
    const htmlCode = extractHTMLCode(rawCode)
    generatedCode.value = htmlCode
    
    let previewHTML = htmlCode.trim()
    
    if (!previewHTML.startsWith('<!DOCTYPE') && !previewHTML.startsWith('<html')) {
      const bodyMatch = previewHTML.match(/<body([^>]*)>([\s\S]*?)<\/body>/i)
      if (bodyMatch) {
        previewHTML = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head><body${bodyMatch[1]}>${bodyMatch[2]}</body></html>`
      } else {
        previewHTML = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head><body>${previewHTML}</body></html>`
      }
    }
    
    previewContent.value = previewHTML
    return
  }
  
  const htmlTagMatch = content.match(/<html[\s\S]*<\/html>/)
  if (htmlTagMatch) {
    generatedCode.value = htmlTagMatch[0]
    previewContent.value = htmlTagMatch[0]
    return
  }
  
  const bodyTagMatch = content.match(/<body[\s\S]*<\/body>/)
  if (bodyTagMatch) {
    generatedCode.value = bodyTagMatch[0]
    previewContent.value = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>${bodyTagMatch[0]}</html>`
    return
  }
  
  generatedCode.value = ''
  previewContent.value = ''
}

const handleEnter = (e) => {
  if (e.ctrlKey || e.metaKey) {
    return
  }
  e.preventDefault()
  if (isLoading.value) return
  sendMessage()
}

const stopGeneration = () => {
  const controller = activeRequestController.value
  if (!controller) return
  controller.userAborted = true
  controller.abort()
  isThinking.value = false
  isPreviewLoading.value = false
  isLoading.value = false
  showToast('已停止生成')
}

const sendMessage = async () => {
  if (isLoading.value || (!chatInput.value.trim() && pendingFiles.value.length === 0)) return
  
  const userContent = chatInput.value.trim() || '请分析附件内容，并给出可执行的结论。'
  const selectedFiles = [...pendingFiles.value]
  isLoading.value = true
  const controller = new AbortController()
  activeRequestController.value = controller
  const { attachments, skipped } = await prepareAttachments(selectedFiles, controller.signal)
  if (controller.signal.aborted) {
    if (activeRequestController.value === controller) activeRequestController.value = null
    return
  }
  if (selectedFiles.length > 0 && attachments.length === 0) {
    isLoading.value = false
    activeRequestController.value = null
    showToast(skipped[0] || '没有可分析的文本文件')
    return
  }
  
  messages.value.push({
    role: 'user',
    content: userContent,
    attachments: attachments.map(item => item.path),
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  chatInput.value = ''
  pendingFiles.value = []
  if (skipped.length) showToast(`已跳过 ${skipped.length} 个不支持或过大的文件`)
  scrollToBottom()
  
  const thinkingCompleted = await simulateThinking(controller.signal)
  if (!thinkingCompleted) {
    if (activeRequestController.value === controller) activeRequestController.value = null
    thinkingSteps.value = []
    return
  }
  
  messages.value.push({
    role: 'thinking',
    steps: [...thinkingSteps.value],
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  thinkingSteps.value = []
  
  await callChatAPI(attachments, controller)
  
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
  const initialMessage = route.query.message
  if (!initialMessage) {
    if (route.query.skillName) chatInput.value = `请使用「${route.query.skillName}」处理：`
    return
  }
  
  chatInput.value = initialMessage
  await sendMessage()
})

onUnmounted(() => {
  if (activeRequestController.value) {
    activeRequestController.value.userAborted = true
    activeRequestController.value.abort()
  }
  document.removeEventListener('click', closeMenu)
  stopPreviewResize()
})
</script>

<template>
  <div class="dashboard">
    <SiteHeader />

    <main class="main">
      <div class="left-panel">
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
                  <div v-if="isThinking" class="thinking-dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
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
                <div class="message-text" v-html="message.content"></div>
                <div v-if="message.attachments?.length" class="message-attachments">
                  <span v-for="path in message.attachments" :key="path" class="message-attachment">📄 {{ path }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>

        <div class="chat-input-area">
          <div v-if="pendingFiles.length" class="pending-attachments">
            <div v-for="(item, index) in pendingFiles" :key="`${item.path}-${item.file.lastModified}`" class="pending-attachment">
              <span class="pending-attachment-icon">{{ item.file.type.startsWith('text/') ? '📄' : '📎' }}</span>
              <span class="pending-attachment-name" :title="item.path">{{ item.path }}</span>
              <button class="pending-attachment-remove" type="button" :aria-label="`移除 ${item.path}`" @click="removePendingFile(index)">×</button>
            </div>
          </div>
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

            <button
              :class="['send-btn', { stop: isLoading }]"
              :aria-label="isLoading ? '终止生成' : '发送消息'"
              :title="isLoading ? '终止生成' : '发送消息'"
              @click="isLoading ? stopGeneration() : sendMessage()"
            >
              <svg v-if="isLoading" width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <rect x="7" y="7" width="10" height="10" rx="1"></rect>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div
        ref="rightPanelRef"
        class="right-panel"
        :class="{ resizing: isResizingPreview }"
        :style="{ gridTemplateRows: `${previewHeightPercent}% 10px minmax(0, 1fr)` }"
      >
        <div class="preview-panel">
          <div class="panel-header">
            <span class="panel-title">预览</span>
            <span class="panel-status">{{ previewContent ? '已生成' : '等待中' }}</span>
          </div>
          <div class="panel-content">
            <div v-if="isPreviewLoading" class="preview-loading">
              <div class="spinner"></div>
              <p class="loading-text">代码生成中...</p>
            </div>
            <iframe v-else-if="previewContent" class="preview-iframe" :srcdoc="previewContent"></iframe>
            <div v-else class="preview-placeholder">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="preview-icon">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
              <p class="preview-text">Web 应用预览将在此显示</p>
              <p class="preview-hint">点击发送消息开始构建应用</p>
            </div>
          </div>
        </div>

        <div
          class="panel-resizer"
          :class="{ active: isResizingPreview }"
          role="separator"
          aria-label="调整预览区和代码区高度"
          aria-orientation="horizontal"
          :aria-valuenow="Math.round(previewHeightPercent)"
          @pointerdown="startPreviewResize"
        >
          <span class="resizer-handle"></span>
        </div>

        <div class="code-panel">
          <div class="panel-header">
            <span class="panel-title">代码</span>
            <div class="panel-actions">
              <button class="copy-btn" @click="copyCode">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
                <span>复制</span>
              </button>
            </div>
          </div>
          <div class="panel-content code-content">
            <pre v-if="generatedCode" class="code-block"><code class="code-text">{{ generatedCode }}</code></pre>
            <pre v-else class="code-block"><code class="code-text">// 生成的代码将在此显示

发送消息开始生成作品代码...</code></pre>
          </div>
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
  padding: 24px;
  box-sizing: border-box;
  background: #f5f5f5;
  height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: minmax(320px, 0.72fr) minmax(0, 1.28fr);
  gap: 20px;
}

.left-panel {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.right-panel {
  min-width: 0;
  min-height: 0;
  display: grid;
  gap: 0;
  overflow: hidden;
}

@media (max-width: 768px) {
  .main {
    display: block;
    padding: 16px;
  }

  .right-panel {
    display: none;
  }
}

.chat-container {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-panel,
.code-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
  min-height: 0;
}

.preview-panel {
  height: 100%;
}

.panel-resizer {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 10px;
  cursor: row-resize;
  touch-action: none;
  z-index: 2;
}

.panel-resizer::before {
  content: '';
  position: absolute;
  inset: 3px 0;
  background: #e2e8f0;
  transition: background 0.2s;
}

.resizer-handle {
  position: relative;
  width: 52px;
  height: 4px;
  border-radius: 999px;
  background: #94a3b8;
  transition: width 0.2s, background 0.2s;
}

.panel-resizer:hover::before,
.panel-resizer.active::before {
  background: #bfdbfe;
}

.panel-resizer:hover .resizer-handle,
.panel-resizer.active .resizer-handle {
  width: 68px;
  background: #3b82f6;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

.code-panel {
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.panel-status {
  font-size: 12px;
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #f0f0f0;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: background 0.2s;
}

.copy-btn:hover {
  background: #e0e0e0;
}

.panel-content {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  overflow-y: auto;
  padding: 16px;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.preview-content {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.preview-iframe {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 0;
  border: none;
  background: white;
}

.right-panel.resizing .preview-iframe {
  pointer-events: none;
}

.preview-icon {
  color: #cbd5e1;
  margin-bottom: 12px;
}

.preview-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.preview-hint {
  font-size: 12px;
  color: #999;
}

.code-content {
  background: #1e293b;
  padding: 0;
  min-height: 0;
  overflow: auto;
}

.code-block {
  display: block;
  margin: 0;
  padding: 22px 24px;
  width: 100%;
  min-width: max-content;
  min-height: 100%;
  box-sizing: border-box;
  background: #1e293b;
  border-radius: 0;
  overflow-x: auto;
  tab-size: 2;
}

.code-text {
  display: block;
  width: max-content;
  min-width: 100%;
  padding: 0;
  border-radius: 0;
  background: transparent;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.75;
  color: #f8fafc;
  white-space: pre;
  text-shadow: none;
}

@media (min-width: 1440px) {
  .main {
    padding-left: 32px;
    padding-right: 32px;
    grid-template-columns: minmax(360px, 0.68fr) minmax(0, 1.32fr);
  }

  .code-text {
    font-size: 16px;
  }
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

.message-text pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  margin: 12px 0;
}

.message-text code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.message-text pre code {
  background: none;
  padding: 0;
  color: inherit;
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

.thinking-dots {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0;
}

.thinking-dots .dot {
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
  animation: thinking-bounce 1.4s infinite ease-in-out both;
}

.thinking-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.thinking-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes thinking-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input-area {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  width: calc(100% - 52px);
  margin-left: 52px;
  box-sizing: border-box;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border: 1px solid #e5e5e5;
  flex-shrink: 0;
}

.pending-attachments {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 10px;
  margin-bottom: 10px;
  border-bottom: 1px solid #eef0f4;
}

.pending-attachment {
  max-width: 240px;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 8px;
  border: 1px solid #dbe3f0;
  border-radius: 9px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  flex: 0 0 auto;
}

.pending-attachment-name {
  max-width: 174px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-attachment-remove {
  width: 20px;
  height: 20px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
  cursor: pointer;
  line-height: 20px;
}

.pending-attachment-remove:hover {
  background: #fecaca;
  color: #b91c1c;
}

.message-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.message-attachment {
  max-width: 100%;
  padding: 5px 8px;
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.send-btn.stop {
  background: #ef4444;
}

.send-btn.stop:hover {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35);
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
