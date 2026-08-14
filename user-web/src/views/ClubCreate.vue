<template>
  <div class="page">
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/clubs">社团广场</router-link>
      </div>
    </nav>
    <div class="form-card">
      <h2>申请创建社团</h2>
      <el-form :model="form" label-width="90px">
        <el-form-item label="社团名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="form.category" placeholder="选择类别">
            <el-option label="学术科技" value="ACADEMIC" />
            <el-option label="文化艺术" value="ART" />
            <el-option label="体育竞技" value="SPORTS" />
            <el-option label="志愿服务" value="VOLUNTEER" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
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
const form = reactive({ name: '', category: '', description: '' })

const handleSubmit = async () => {
  if (!form.name || !form.category) {
    ElMessage.warning('请填写名称和类别')
    return
  }
  await request.post('/club/apply', form)
  ElMessage.success('申请已提交，等待审核')
  router.push('/clubs')
}
</script>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: #2E5BFF; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: #6B7280; }
.form-card { background: #fff; padding: 32px; border-radius: 12px; max-width: 600px; margin: 40px auto; }
h2 { margin-bottom: 24px; }
</style>
