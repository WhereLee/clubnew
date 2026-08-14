<template>
  <div class="page">
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <router-link to="/login" class="nav-link">登录</router-link>
    </nav>
    <div class="form-card">
      <h2>注册账号</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = reactive({ username: '', nickname: '', password: '', phone: '', email: '' })

const handleRegister = async () => {
  if (!form.username || !form.password || !form.nickname) {
    ElMessage.warning('请填写用户名、昵称和密码')
    return
  }
  await request.post('/auth/register', form)
  ElMessage.success('注册成功，请登录')
  router.push('/login')
}
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-link { text-decoration: none; color: #6B7280; }
.form-card { background: #fff; padding: 32px; border-radius: 12px; max-width: 460px; margin: 60px auto; }
h2 { margin-bottom: 24px; }
</style>
