<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import ecommerceImg from '../assets/images/ecommerce.jpg'
import corporateImg from '../assets/images/corporate.jpg'
import blogImg from '../assets/images/blog.jpg'
import adminImg from '../assets/images/admin.jpg'
import socialImg from '../assets/images/social.jpg'
import educationImg from '../assets/images/education.jpg'

const router = useRouter()
const activeTab = ref('discover')

const chatInput = ref('')
const activeMode = ref('工程师')

const modes = ref([
  { name: '工程师', icon: '🧑‍💻' },
  { name: '团队', icon: '👥' },
  { name: '深度研究', icon: '🔍' }
])

const navItems = [
  { name: 'discover', label: '发现' },
  { name: 'templates', label: '模板' },
  { name: 'profile', label: '我的' }
]

const showMenu = ref(false)
const menuPosition = ref({ left: '0px', bottom: '0px' })
const menuBtnRef = ref(null)
const teamMode = ref(false)
const attachmentMode = ref(false)
const deepResearch1 = ref(false)
const deepResearch2 = ref(false)

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

import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})

const toggleSwitch = (state) => {
  state.value = !state.value
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

const handleEnter = (e) => {
  if (e.ctrlKey || e.metaKey) {
    return
  }
  e.preventDefault()
  handleSend()
}

const handleSend = () => {
  const message = chatInput.value.trim() || '请Alex构建一个Web应用。'
  router.push({ path: '/chat', query: { message } })
}

const aiTeam = ref([
  {
    name: 'Iris',
    role: 'Deep Researcher',
    desc: 'Finds real demand and niches with Deep Research, then turns signals into a focused opportunity.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Iris'
  },
  {
    name: 'Bob',
    role: 'Architect',
    desc: 'Designs the system blueprint, choosing the right structure, so your app is scalable and reliable.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob'
  },
  {
    name: 'Emma',
    role: 'Product Manager',
    desc: 'Turns your idea into a clear spec and scope, so the build stays simple and usable.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Emma'
  },
  {
    name: 'Alex',
    role: 'Engineer',
    desc: 'Builds a production-ready, full-stack app by wiring the frontend, backend, integrations, and deployment.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex'
  },
  {
    name: 'Sarah',
    role: 'SEO Specialist',
    desc: 'Launch SEO pages fast and automate optimizations to drive organic traffic quickly at a much lower cost.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah'
  },
  {
    name: 'David',
    role: 'Data Analyst',
    desc: 'Analyzes massive data to spot growth opportunities and surface clear insights so you can make smarter decisions.',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=David'
  }
])

const features = ref([
  {
    title: 'Visual Editor',
    desc: 'Bring your exact design to life. A visual editor to adjust layouts and components quickly.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20visual%20editor%20interface%20for%20web%20design%20with%20drag%20and%20drop%20features%20professional%20UI&image_size=landscape_4_3',
    tag: 'Design'
  },
  {
    title: 'Atoms Cloud',
    desc: 'Powers your apps with a full-stack backend including user login, database, integrations, and scalable hosting.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cloud%20infrastructure%20technology%20illustration%20with%20database%20and%20server%20icons%20modern%20flat%20design&image_size=landscape_4_3',
    tag: 'Backend'
  },
  {
    title: 'Race Mode',
    desc: 'Run your prompt across multiple models to instantly get the best version.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=AI%20model%20comparison%20race%20speed%20competition%20technology%20visualization%20futuristic&image_size=landscape_4_3',
    tag: 'AI'
  },
  {
    title: 'SEO Agent',
    desc: 'Automatically make your site search-engine-friendly, so Google can crawl, index, and rank pages.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=SEO%20search%20engine%20optimization%20analytics%20dashboard%20with%20growth%20charts%20professional&image_size=landscape_4_3',
    tag: 'Growth'
  },
  {
    title: 'Instant AI Integrations',
    desc: 'Add powerful AI to your products with models like Gemini and GPT - no API keys and no setup.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=AI%20integration%20api%20connection%20technology%20interface%20modern%20clean%20design&image_size=landscape_4_3',
    tag: 'AI'
  },
  {
    title: 'Ads Specialist',
    desc: 'Scale growth with less manual work. Ads specialist handles campaign creation, tracking, and optimization.',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=digital%20advertising%20campaign%20management%20dashboard%20analytics%20charts%20business&image_size=landscape_4_3',
    tag: 'Growth'
  }
])

const products = ref([
  {
    name: 'Investment Portfolio Monitor',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=investment%20portfolio%20dashboard%20with%20charts%20and%20financial%20data%20professional%20UI&image_size=landscape_16_9',
    category: 'Finance'
  },
  {
    name: 'AI-Powered Manga App',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=colorful%20manga%20app%20interface%20with%20comic%20style%20design%20vibrant&image_size=landscape_16_9',
    category: 'Entertainment'
  },
  {
    name: 'Fitness App',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fitness%20app%20workout%20tracking%20interface%20with%20exercise%20icons%20clean%20modern&image_size=landscape_16_9',
    category: 'Health'
  }
])

const templates = ref([
  {
    name: '电商平台模板',
    desc: '完整的电商购物流程，支持商品展示、购物车、订单管理',
    image: ecommerceImg,
    category: '电商'
  },
  {
    name: '企业官网模板',
    desc: '专业的企业展示网站，包含首页、关于我们、产品服务等页面',
    image: corporateImg,
    category: '企业'
  },
  {
    name: '博客系统模板',
    desc: '功能完善的博客平台，支持文章发布、评论、分类管理',
    image: blogImg,
    category: '内容'
  },
  {
    name: '后台管理模板',
    desc: '现代化的管理后台界面，包含数据看板、用户管理、权限控制',
    image: adminImg,
    category: '管理'
  },
  {
    name: '社交媒体模板',
    desc: '社交网络平台模板，支持用户动态、关注、消息功能',
    image: socialImg,
    category: '社交'
  },
  {
    name: '在线教育模板',
    desc: '在线学习平台模板，包含课程展示、视频播放、学习进度',
    image: educationImg,
    category: '教育'
  }
])

const stats = ref([
  { value: '50K+', label: 'GitHub Stars' },
  { value: '#1', label: 'Product of the Week' },
  { value: '30+', label: 'Academic Papers' },
  { value: '10K+', label: 'Active Users' }
])

const myProjects = ref([
  { name: '我的第一个项目', status: '开发中', updated: '2小时前' },
  { name: '电商网站', status: '已完成', updated: '1天前' },
  { name: '个人博客', status: '开发中', updated: '3天前' }
])
</script>

<template>
  <div class="dashboard">
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">⚛</span>
          <span class="logo-text">Atoms</span>
        </div>
        <nav class="nav">
          <button 
            v-for="item in navItems" 
            :key="item.name"
            :class="['nav-item', { active: activeTab === item.name }]"
            @click="activeTab = item.name"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>
    </header>

    <main class="main">
      <template v-if="activeTab === 'discover'">
        <section class="home-container">
          <div class="hero-section">
            <h1 class="hero-title">输入想法，产出产品。</h1>
          </div>

          <div class="input-section">
            <div class="input-card">
              <textarea 
                v-model="chatInput" 
                class="main-input" 
                placeholder="请Alex构建一个Web应用。"
                rows="3"
                @keydown.enter="handleEnter"
              ></textarea>

              <div class="input-toolbar">
                <div class="toolbar-left">
                  <button ref="menuBtnRef" class="toolbar-btn menu-toggle-btn" @click="toggleMenu">
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
                </div>
                
                <div class="toolbar-right">
                  <button class="send-btn" @click="handleSend">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="22" y1="2" x2="11" y2="13"></line>
                      <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>

      <template v-if="activeTab === 'templates'">
        <section class="section">
          <div class="section-header">
            <h2 class="section-title">精选模板</h2>
            <p class="section-desc">专业设计的可复用模板，快速开始你的项目</p>
          </div>
          <div class="templates-grid">
            <div v-for="template in templates" :key="template.name" class="template-card">
              <div class="template-image">
                <img :src="template.image" :alt="template.name" />
                <span class="template-category">{{ template.category }}</span>
              </div>
              <h3 class="template-name">{{ template.name }}</h3>
              <p class="template-desc">{{ template.desc }}</p>
              <van-button size="small" type="primary" block>使用模板</van-button>
            </div>
          </div>
        </section>
      </template>

      <template v-if="activeTab === 'profile'">
        <section class="section">
          <div class="profile-card">
            <div class="profile-header">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=User" class="profile-avatar" />
              <div class="profile-info">
                <h2 class="profile-name">用户昵称</h2>
                <p class="profile-desc">欢迎使用 Atoms</p>
              </div>
            </div>
          </div>

          <div class="quick-actions">
            <h3 class="quick-title">快速开始</h3>
            <div class="quick-grid">
              <div class="quick-item">
                <span class="quick-icon">+</span>
                <span class="quick-text">新建项目</span>
              </div>
              <div class="quick-item">
                <span class="quick-icon">📋</span>
                <span class="quick-text">复制项目</span>
              </div>
              <div class="quick-item">
                <span class="quick-icon">📤</span>
                <span class="quick-text">导入代码</span>
              </div>
            </div>
          </div>

          <div class="my-projects">
            <div class="section-header">
              <h3 class="section-title">我的项目</h3>
              <van-button text size="small">查看全部</van-button>
            </div>
            <div class="projects-list">
              <div v-for="project in myProjects" :key="project.name" class="project-item">
                <div class="project-info">
                  <h4 class="project-name">{{ project.name }}</h4>
                  <span class="project-status">{{ project.status }}</span>
                </div>
                <span class="project-updated">{{ project.updated }}</span>
              </div>
            </div>
          </div>

          <div class="settings-section">
            <h3 class="section-title">账户设置</h3>
            <div class="settings-list">
              <div class="settings-item">
                <span class="settings-icon">📁</span>
                <span class="settings-text">我的项目</span>
                <span class="settings-arrow">›</span>
              </div>
              <div class="settings-item">
                <span class="settings-icon">⭐</span>
                <span class="settings-text">我的收藏</span>
                <span class="settings-arrow">›</span>
              </div>
              <div class="settings-item">
                <span class="settings-icon">⚙</span>
                <span class="settings-text">账户设置</span>
                <span class="settings-arrow">›</span>
              </div>
              <div class="settings-item">
                <span class="settings-icon">❓</span>
                <span class="settings-text">帮助中心</span>
                <span class="settings-arrow">›</span>
              </div>
            </div>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  background: #f5f5f5;
  width: 100%;
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
}

.logo-icon {
  font-size: clamp(1.5rem, 3vw, 2rem);
}

.logo-text {
  font-size: clamp(1.2rem, 2.5vw, 1.6rem);
  font-weight: 700;
  color: #333;
}

.nav {
  display: flex;
  gap: 3vw;
}

.nav-item {
  background: none;
  border: none;
  font-size: clamp(0.85rem, 1.5vw, 1rem);
  color: #666;
  cursor: pointer;
  padding: 0.5vw 0;
  position: relative;
  transition: color 0.2s;
}

.nav-item:hover {
  color: #667eea;
}

.nav-item.active {
  color: #667eea;
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #667eea;
  border-radius: 2px;
}

.header-actions {
  display: flex;
  gap: 1vw;
}

.main {
  width: 100%;
  padding: 100px 3vw 3vw;
  box-sizing: border-box;
  background: #f5f5f5;
}

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 1.5vw;
  padding: 8vw 5vw;
  text-align: center;
  color: white;
  margin-bottom: 5vw;
  width: 100%;
  box-sizing: border-box;
}

.hero-title {
  font-size: clamp(1.8rem, 4vw, 3rem);
  font-weight: 700;
  margin: 0 0 2vw;
  line-height: 1.2;
}

.hero-desc {
  font-size: clamp(0.9rem, 1.5vw, 1.2rem);
  opacity: 0.9;
  margin: 0 0 4vw;
  max-width: 60vw;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 2vw;
}

.section {
  margin-bottom: 5vw;
}

.section-header {
  margin-bottom: 2.5vw;
}

.section-title {
  font-size: clamp(1.2rem, 2.5vw, 1.8rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 0.8vw;
}

.section-desc {
  font-size: clamp(0.85rem, 1.2vw, 1rem);
  color: #666;
  margin: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5vw;
}

.product-card {
  background: white;
  border-radius: 1.2vw;
  overflow: hidden;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-0.5vw);
  box-shadow: 0 0.8vw 2.5vw rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
}

.product-image img {
  width: 100%;
  height: 20vw;
  object-fit: cover;
}

.product-category {
  position: absolute;
  top: 1vw;
  left: 1vw;
  background: rgba(255, 255, 255, 0.95);
  padding: 0.4vw 1vw;
  border-radius: 1.5vw;
  font-size: clamp(0.7rem, 1vw, 0.85rem);
  font-weight: 600;
  color: #667eea;
}

.product-name {
  padding: 1.5vw;
  margin: 0;
  font-size: clamp(0.9rem, 1.3vw, 1.1rem);
  font-weight: 600;
  color: #333;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5vw;
}

.team-card {
  background: white;
  border-radius: 1.2vw;
  padding: 2.5vw;
  text-align: center;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
}

.team-avatar {
  width: 12vw;
  height: 12vw;
  border-radius: 50%;
  margin-bottom: 1.5vw;
}

.team-name {
  font-size: clamp(1rem, 1.5vw, 1.2rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 0.5vw;
}

.team-role {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #667eea;
  font-weight: 600;
  display: block;
  margin-bottom: 1.2vw;
}

.team-desc {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5vw;
}

.feature-card {
  background: white;
  border-radius: 1.2vw;
  padding: 2.5vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
}

.feature-tag {
  display: inline-block;
  background: #f0f0f0;
  padding: 0.4vw 0.8vw;
  border-radius: 0.4vw;
  font-size: clamp(0.7rem, 1vw, 0.8rem);
  font-weight: 600;
  color: #666;
  margin-bottom: 1.2vw;
}

.feature-title {
  font-size: clamp(1rem, 1.5vw, 1.2rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 0.8vw;
}

.feature-desc {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #666;
  margin: 0 0 1.5vw;
  line-height: 1.5;
}

.feature-image {
  width: 100%;
  height: 18vw;
  border-radius: 0.8vw;
  object-fit: cover;
  margin-bottom: 1.5vw;
}

.stats-section {
  background: white;
  border-radius: 1.2vw;
  padding: 4vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 3vw;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: clamp(1.5rem, 3vw, 2.2rem);
  font-weight: 700;
  color: #667eea;
  margin-bottom: 0.8vw;
}

.stat-label {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #666;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.5vw;
}

.template-card {
  background: white;
  border-radius: 1.2vw;
  overflow: hidden;
  padding: 2vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
}

.template-image {
  position: relative;
  margin-bottom: 1.5vw;
}

.template-image img {
  width: 100%;
  height: 18vw;
  border-radius: 0.8vw;
  object-fit: cover;
}

.template-category {
  position: absolute;
  top: 0.8vw;
  left: 0.8vw;
  background: rgba(255, 255, 255, 0.95);
  padding: 0.4vw 0.8vw;
  border-radius: 1.2vw;
  font-size: clamp(0.7rem, 1vw, 0.8rem);
  font-weight: 600;
  color: #667eea;
}

.template-name {
  font-size: clamp(1rem, 1.5vw, 1.2rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 0.8vw;
}

.template-desc {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #666;
  margin: 0 0 1.5vw;
  line-height: 1.5;
}

.profile-card {
  background: white;
  border-radius: 1.2vw;
  padding: 3vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
  margin-bottom: 2.5vw;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 2.5vw;
}

.profile-avatar {
  width: 15vw;
  height: 15vw;
  border-radius: 50%;
}

.profile-name {
  font-size: clamp(1.2rem, 2vw, 1.5rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 0.5vw;
}

.profile-desc {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  color: #666;
  margin: 0;
}

.quick-actions {
  background: white;
  border-radius: 1.2vw;
  padding: 2.5vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
  margin-bottom: 2.5vw;
}

.quick-title {
  font-size: clamp(1rem, 1.5vw, 1.2rem);
  font-weight: 700;
  color: #333;
  margin: 0 0 1.5vw;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5vw;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2vw;
  background: #f9f9f9;
  border-radius: 1vw;
  cursor: pointer;
  transition: background 0.2s;
}

.quick-item:hover {
  background: #f0f0f0;
}

.quick-icon {
  font-size: clamp(1.5rem, 2.5vw, 2rem);
  margin-bottom: 0.8vw;
}

.quick-text {
  font-size: clamp(0.85rem, 1.2vw, 1rem);
  color: #333;
  font-weight: 600;
}

.my-projects {
  background: white;
  border-radius: 1.2vw;
  padding: 2.5vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
  margin-bottom: 2.5vw;
}

.projects-list {
  display: flex;
  flex-direction: column;
  gap: 1.5vw;
}

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5vw;
  background: #f9f9f9;
  border-radius: 1vw;
}

.project-info {
  display: flex;
  align-items: center;
  gap: 1.2vw;
}

.project-name {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  font-weight: 600;
  color: #333;
  margin: 0;
}

.project-status {
  font-size: clamp(0.75rem, 1vw, 0.85rem);
  padding: 0.4vw 0.8vw;
  border-radius: 1vw;
}

.project-status:nth-child(2) {
  background: #e6f7ff;
  color: #1890ff;
}

.project-status:nth-child(3) {
  background: #f6ffed;
  color: #52c41a;
}

.project-updated {
  font-size: clamp(0.8rem, 1.1vw, 0.9rem);
  color: #999;
}

.settings-section {
  background: white;
  border-radius: 1.2vw;
  padding: 2.5vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.08);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 0.8vw;
}

.settings-item {
  display: flex;
  align-items: center;
  padding: 1.5vw;
  border-radius: 1vw;
  cursor: pointer;
  transition: background 0.2s;
}

.settings-item:hover {
  background: #f9f9f9;
}

.settings-icon {
  font-size: clamp(1rem, 1.5vw, 1.3rem);
  margin-right: 1.2vw;
}

.settings-text {
  flex: 1;
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  color: #333;
}

.settings-arrow {
  font-size: clamp(1rem, 1.5vw, 1.3rem);
  color: #ccc;
}

@media (max-width: 992px) {
  .product-grid,
  .team-grid,
  .features-grid,
  .templates-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .hero-title {
    font-size: clamp(1.5rem, 5vw, 2.2rem);
  }
  
  .header-content {
    padding: 2vw 4vw;
  }
  
  .nav {
    gap: 4vw;
  }
}

@media (max-width: 576px) {
  .product-grid,
  .team-grid,
  .features-grid,
  .templates-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .hero-title {
    font-size: clamp(1.2rem, 6vw, 1.8rem);
  }
  
  .hero {
    padding: 6vw 4vw;
  }
  
  .header-content {
    padding: 3vw 5vw;
  }
  
  .nav {
    gap: 5vw;
  }
  
  .main {
    padding: 12vw 4vw 4vw;
  }
  
  .hero-desc {
    max-width: 100%;
  }
}

.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  box-sizing: border-box;
  background: #f5f5f5;
}

.hero-section {
  text-align: center;
  margin-bottom: 48px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 16px;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.hero-subtitle {
  font-size: 20px;
  color: #666;
  margin: 0;
}

.input-section {
  width: 100%;
  max-width: 700px;
}

.input-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e5e5;
  padding: 16px;
}

.main-input {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  color: #333;
  background: transparent;
  margin-bottom: 12px;
  line-height: 1.6;
  min-height: 64px;
}

.main-input::placeholder {
  color: #999;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f8f9fa;
  border: none;
  border-radius: 8px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.toolbar-btn:hover {
  background: #f0f0f0;
}

.close-btn {
  padding: 8px;
  background: transparent;
}

.close-btn:hover {
  background: #f0f0f0;
}

.btn-label {
  font-size: 14px;
  color: #333;
}

.dropdown-menu {
  position: fixed;
  top: auto;
  bottom: auto;
  left: auto;
  right: auto;
  min-width: 220px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid #e5e5e5;
  padding: 8px 0;
  z-index: 9999;
}

.theme-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
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

.send-btn {
  width: 40px;
  height: 40px;
  background: #667eea;
  border: none;
  border-radius: 10px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover {
  background: #5568d3;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .input-card {
    padding: 12px;
  }
  
  .dropdown-menu {
    min-width: 100%;
  }
}
</style>