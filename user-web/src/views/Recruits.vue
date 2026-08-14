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
      <h2>纳新广场</h2>
      <div class="grid">
        <el-card v-for="r in list" :key="r.id" shadow="hover" class="recruit-card">
          <h3>{{ r.title }}</h3>
          <p>{{ r.description }}</p>
          <div class="meta">
            <span>名额：{{ r.appliedCount }}/{{ r.quota }}</span>
            <el-tag :type="r.status === 'IN_PROGRESS' ? 'success' : 'info'">{{ r.status }}</el-tag>
          </div>
          <el-button v-if="r.status === 'IN_PROGRESS'" type="primary" size="small" @click="apply(r)">报名</el-button>
        </el-card>
      </div>
      <el-empty v-if="!list.length" description="暂无纳新活动" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref<any[]>([])

const load = async () => {
  const res: any = await request.get('/recruit/list', { params: { pageNum: 1, pageSize: 100 } })
  list.value = res.data.records
}
const apply = async (r: any) => {
  await request.post(`/recruit/${r.id}/apply`)
  ElMessage.success('报名成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: #6B7280; }
.content { max-width: 1000px; margin: 24px auto; }
h2 { margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.recruit-card h3 { margin-bottom: 8px; }
.meta { display: flex; justify-content: space-between; align-items: center; margin: 12px 0; }
</style>
