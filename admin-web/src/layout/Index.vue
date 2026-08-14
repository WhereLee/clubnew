<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">社团管理</div>
      <el-menu :default-active="$route.path" router background-color="#1B1F2A" text-color="#9CA3AF" active-text-color="#FFFFFF">
        <el-menu-item index="/dashboard"><el-icon><Monitor /></el-icon>工作台</el-menu-item>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
          <el-menu-item index="/system/dict">字典管理</el-menu-item>
          <el-menu-item index="/system/config">参数配置</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="club">
          <template #title><el-icon><Guide /></el-icon>社团管理</template>
          <el-menu-item index="/club/list">社团列表</el-menu-item>
          <el-menu-item index="/club/audit">社团审批</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="biz">
          <template #title><el-icon><Grid /></el-icon>业务管理</template>
          <el-menu-item index="/recruit/list">纳新管理</el-menu-item>
          <el-menu-item index="/activity/list">活动管理</el-menu-item>
          <el-menu-item index="/fund/audit">经费审批</el-menu-item>
          <el-menu-item index="/rank">排行榜</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="log">
          <template #title><el-icon><Document /></el-icon>日志</template>
          <el-menu-item index="/log/oper">操作日志</el-menu-item>
          <el-menu-item index="/log/login">登录日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title }}</span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">管理员 <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { logout } from '../api/auth'

const router = useRouter()
const handleCommand = async (cmd: string) => {
  if (cmd === 'logout') {
    try { await logout() } catch {}
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar { background: #1B1F2A; overflow-y: auto; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: 600; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; }
.main { background: #F5F6FA; padding: 24px; }
.user-info { cursor: pointer; display: flex; align-items: center; gap: 4px; }
</style>
