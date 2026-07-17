<script setup>
import { ref } from 'vue'

const showToast = () => {
  Toast('首页！')
}

const count = ref(0)
</script>

<template>
  <div style="padding: 20px;">
    <van-button type="primary" @click="showToast">主要按钮</van-button>
    <van-button type="success" style="margin-left: 10px;">成功按钮</van-button>
    <van-button type="warning" style="margin-left: 10px;">警告按钮</van-button>
    <van-button type="danger" style="margin-left: 10px;">危险按钮</van-button>

    <van-cell-group style="margin-top: 20px;">
      <van-cell title="单元格" value="点击查看" @click="showToast" />
      <van-cell title="计数器" :value="count">
        <template #right-icon>
          <van-stepper v-model="count" />
        </template>
      </van-cell>
    </van-cell-group>

    <van-card title="Vant 组件库" desc="已成功引入" style="margin-top: 20px;">
      <template #tags>
        <van-tag type="primary">Vue 3</van-tag>
        <van-tag type="success">Vant 4</van-tag>
      </template>
    </van-card>
  </div>
</template>