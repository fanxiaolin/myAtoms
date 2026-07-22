<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '../components/SiteHeader.vue'
const router = useRouter(); const items = ref([]); const query = ref(''); const category = ref('全部'); const message = ref('')
const load = async () => { items.value = (await (await fetch('/api/skills')).json()).items || [] }
onMounted(load)
const categories = computed(() => ['全部', ...new Set(items.value.map(i => i.category))])
const visible = computed(() => items.value.filter(i =>
  (category.value === '全部' || i.category === category.value) &&
  (!query.value.trim() || [i.name, i.description, ...i.keywords].join(' ').toLowerCase().includes(query.value.toLowerCase()))))
const install = async (item) => { await fetch(`/api/skills/${item.id}/install`, { method:'POST' }); message.value = `「${item.name}」已安装并启用`; await load() }
const uninstall = async (item) => { await fetch(`/api/skills/${item.id}`, { method:'DELETE' }); await load() }
const useSkill = (item) => router.push({ path:'/chat', query:{ skill:item.id,
  skillName:item.name, mode:['后端', '质量'].includes(item.category) ? 'team' : 'engineer' } })
</script>
<template><div class="page"><SiteHeader />
<main><div class="hero"><span>PRODUCT DEMO TOOLKIT</span><h1>Skill 广场</h1><p>选择网页与应用生成能力，把产品想法快速变成可运行、可交互、可预览的 Demo。</p></div>
<div v-if="message" class="notice">{{ message }}</div><div class="filters"><input v-model="query" placeholder="搜索 Skill 或触发词"><div><button v-for="c in categories" :key="c" :class="{active:category===c}" @click="category=c">{{ c }}</button></div></div>
<section class="grid"><article v-for="item in visible" :key="item.id"><i v-if="item.featured">精选</i><div class="agent">{{ item.agent }} · {{ item.category }}</div><h2>{{ item.name }}</h2><p>{{ item.description }}</p><div class="tags"><span v-for="k in item.keywords" :key="k">{{ k }}</span></div><div class="actions"><button v-if="!item.installed" class="primary" @click="install(item)">一键安装</button><template v-else><button class="primary" @click="useSkill(item)">立即使用</button><button @click="uninstall(item)">卸载</button></template></div></article></section></main></div></template>
<style scoped>
.page{min-height:100vh;background:#f6f7fb;color:#172033}header{height:64px;padding:0 5vw;display:flex;align-items:center;justify-content:space-between;background:white;border-bottom:1px solid #e8eaf0}button{border:0;background:none;cursor:pointer}header>button{font-size:19px;font-weight:800}nav{display:flex;gap:28px;align-items:center}nav button,nav b{font-size:14px}main{max-width:1180px;margin:auto;padding:52px 24px}.hero span{color:#5965df;font-size:12px;font-weight:700;letter-spacing:2px}.hero h1{font-size:46px;margin:10px 0}.hero p{color:#687086}.notice{margin:20px 0;padding:12px 16px;background:#e9f8ef;color:#168447;border-radius:10px}.filters{margin:32px 0 20px;display:flex;justify-content:space-between;gap:20px;flex-wrap:wrap}.filters input{width:300px;padding:11px 14px;border:1px solid #dfe2ea;border-radius:10px}.filters button{padding:8px 13px;border-radius:20px;color:#667085}.filters button.active{background:#172033;color:white}.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:18px}article{position:relative;background:white;padding:24px;border:1px solid #e5e7ee;border-radius:18px;box-shadow:0 8px 26px #1b254008}article i{position:absolute;right:18px;top:18px;background:#fff1c7;color:#9a6500;padding:4px 8px;border-radius:12px;font-size:11px}.agent{font-size:12px;color:#5965df}h2{font-size:20px;margin:12px 0}article p{height:44px;color:#687086;font-size:14px;line-height:1.6}.tags{display:flex;gap:6px;flex-wrap:wrap;margin:18px 0}.tags span{font-size:11px;background:#f1f3f8;padding:5px 8px;border-radius:7px}.actions{display:flex;gap:8px}.actions button{padding:10px 14px;border:1px solid #dfe2ea;border-radius:9px}.actions .primary{flex:1;background:#5360df;color:white;border-color:#5360df}@media(max-width:850px){.grid{grid-template-columns:1fr}.hero h1{font-size:36px}}
</style>
