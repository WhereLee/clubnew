<template>
  <div class="dashboard">
    <h2>工作台</h2>
    <div class="cards">
      <el-card v-for="item in stats" :key="item.title" class="stat-card" shadow="hover">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-title">{{ item.title }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const stats = ref([
  { title: '社团总数', value: '--' },
  { title: '待审批', value: '--' },
  { title: '今日活跃', value: '--' },
  { title: '总用户数', value: '--' }
])
</script>

<style scoped>
.dashboard h2 { margin-bottom: 20px; font-size: 20px; }
.cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card { border-radius: 12px; }
.stat-value { font-size: 28px; font-weight: 600; color: var(--primary); }
.stat-title { color: var(--text-secondary); margin-top: 8px; font-size: 14px; }
</style>
