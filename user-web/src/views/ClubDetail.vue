<template>
  <div class="page">
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/clubs">社团广场</router-link>
        <router-link to="/activities">活动广场</router-link>
      </div>
    </nav>
    <div class="detail-card" v-if="club">
      <h2>{{ club.name }}</h2>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="社团编号">{{ club.code }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ club.category }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ club.memberCount }}</el-descriptions-item>
        <el-descriptions-item label="星级">{{ club.starLevel }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ club.status }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ club.description }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="handleApply">申请入社</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const club = ref<any>(null)

const load = async () => {
  const res: any = await request.get(`/club/${route.params.id}`)
  club.value = res.data
}
const handleApply = async () => {
  await request.post('/club/member/apply', { clubId: Number(route.params.id) })
  ElMessage.success('申请已提交')
}
onMounted(load)
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: #6B7280; }
.detail-card { background: #fff; padding: 32px; border-radius: 12px; max-width: 800px; margin: 40px auto; }
h2 { margin-bottom: 20px; }
.actions { margin-top: 20px; }
</style>
