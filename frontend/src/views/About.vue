<script setup>
import { ref } from 'vue'

const list = ref([
  { name: 'Vue 3', desc: '渐进式 JavaScript 框架' },
  { name: 'Vant', desc: '轻量、可靠的移动端组件库' },
  { name: 'Vue Router', desc: 'Vue.js 官方路由管理器' },
  { name: 'Vite', desc: '下一代前端构建工具' }
])
</script>

<template>
  <div style="padding: 20px;">
    <van-nav-bar title="关于我们" left-text="返回" @click-left="router.back()" />
    
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
    >
      <van-cell v-for="item in list" :key="item.name" :title="item.name" :value="item.desc" />
    </van-list>

    <van-button type="primary" block style="margin-top: 20px;" @click="router.push('/')">返回首页</van-button>
  </div>
</template>