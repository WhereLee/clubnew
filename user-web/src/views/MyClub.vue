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
    <div class="content">
      <el-card v-if="club" shadow="never">
        <template #header>我的社团：{{ club.name }}</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="编号">{{ club.code }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ club.status }}</el-descriptions-item>
          <el-descriptions-item label="成员数">{{ club.memberCount }}</el-descriptions-item>
          <el-descriptions-item label="星级">{{ club.starLevel }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-empty v-else description="你还没有加入任何社团" />

      <el-card v-if="club" shadow="never" class="section">
        <template #header>成员管理</template>
        <el-table :data="members" stripe>
          <el-table-column prop="userId" label="用户ID" width="120" />
          <el-table-column prop="memberRole" label="角色" width="120" />
          <el-table-column prop="status" label="状态" width="120" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const club = ref<any>(null)
const members = ref([])

const load = async () => {
  const res: any = await request.get('/club/mine')
  club.value = res.data
  if (res.data) {
    const m: any = await request.get(`/club/${res.data.id}/members`, { params: { pageNum: 1, pageSize: 100 } })
    members.value = m.data.records
  }
}
onMounted(load)
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: #6B7280; }
.content { max-width: 1000px; margin: 24px auto; }
.section { margin-top: 16px; }
</style>
