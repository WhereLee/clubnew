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
      <el-card shadow="never">
        <template #header>个人中心</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
          <el-descriptions-item label="身份">{{ user?.userType }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="actions">
          <el-button type="primary" @click="$router.push('/club/create')">申请创建社团</el-button>
          <el-button @click="$router.push('/my/club')">我的社团</el-button>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const user = ref<any>(null)

const load = async () => {
  const res: any = await request.get('/getInfo')
  user.value = res.data.user
}
const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
onMounted(load)
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: #6B7280; }
.content { max-width: 700px; margin: 24px auto; }
.actions { margin-top: 20px; display: flex; gap: 12px; }
</style>
