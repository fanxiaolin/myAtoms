<script setup>
import { useRoute, useRouter } from 'vue-router'
const router = useRouter(); const route = useRoute()
const items = [{ label:'发现', path:'/' }, { label:'Skill 广场', path:'/skills' }, { label:'知识库', path:'/knowledge' }]
const active = path => path === '/' ? route.path === '/' : route.path.startsWith(path)
</script>
<template><header class="site-header"><button class="brand" @click="router.push('/')"><span>⚛</span><b>Atoms</b></button><nav aria-label="主导航"><button v-for="item in items" :key="item.path" :class="['nav-link',{active:active(item.path)}]" @click="router.push(item.path)">{{item.label}}</button></nav></header></template>
<style scoped>.site-header{height:64px;padding:0 48px;display:flex;align-items:center;justify-content:space-between;box-sizing:border-box;background:#fff;border-bottom:1px solid #e8eaf0;color:#172033}.brand,.nav-link{border:0;background:none;color:inherit;cursor:pointer}.brand{display:flex;align-items:center;gap:8px;font-size:19px}.brand span{font-weight:400}.site-header nav{display:flex;gap:38px}.nav-link{position:relative;padding:22px 0;font-size:14px;color:#5f6675}.nav-link:hover,.nav-link.active{color:#172033}.nav-link.active{font-weight:700}.nav-link.active::after{content:"";position:absolute;left:0;right:0;bottom:0;height:3px;border-radius:3px 3px 0 0;background:#5360df}@media(max-width:640px){.site-header{padding:0 18px}.site-header nav{gap:18px}.brand b{display:none}.nav-link{font-size:13px}}</style>
