<template>
  <div>
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <router-link to="/">返回首页</router-link>
    </nav>
    <div class="content">
      <h2>社团广场</h2>
      <div class="club-grid">
        <el-card v-for="club in clubs" :key="club.id" class="club-card" shadow="hover">
          <h3>{{ club.name }}</h3>
          <p>{{ club.category }}</p>
          <el-tag :type="club.status==='APPROVED'?'success':'warning'">{{ club.status }}</el-tag>
        </el-card>
        <el-empty v-if="clubs.length===0" description="暂无社团" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const clubs = ref<any[]>([])
onMounted(async () => {
  try {
    const res: any = await request.get('/club/list', { params: { pageNum: 1, pageSize: 20 } })
    clubs.value = res.data?.records || []
  } catch {}
})
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.brand { font-size: 18px; font-weight: 600; color: var(--primary); }
.content { padding: 20px; }
h2 { margin-bottom: 20px; }
.club-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.club-card { border-radius: 12px; }
.club-card h3 { margin-bottom: 8px; }
</style>
