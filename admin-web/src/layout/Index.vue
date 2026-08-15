<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">社团管理</div>
      <el-menu :default-active="$route.path" router background-color="#1B1F2A" text-color="#9CA3AF" active-text-color="#FFFFFF">
        <!-- 业务侧菜单：系统管理员可见 -->
        <template v-if="isAdmin">
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
        </template>
        <!-- 运行侧菜单：技术管理员可见（职责分离：admin 管业务，tech_admin 管运行） -->
        <template v-if="isTechAdmin">
          <el-sub-menu index="monitor">
            <template #title><el-icon><Odometer /></el-icon>监控中心</template>
            <el-menu-item index="/monitor/overview">运行概览</el-menu-item>
            <el-menu-item index="/log/oper">操作日志</el-menu-item>
            <el-menu-item index="/log/login">登录日志</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
      <button class="ai-entry" @click="aiVisible = true">
        <span class="ai-entry-orb"></span>
        AI 助手
      </button>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title }}</span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">{{ roleLabel }} <el-icon><ArrowDown /></el-icon></span>
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
  <AiDrawer :visible="aiVisible" @close="aiVisible = false" />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '../api/auth'
import { clearTokens, getUserType } from '../utils/request'

const isAdmin = computed(() => getUserType() === 'ADMIN')
const isTechAdmin = computed(() => getUserType() === 'TECH_ADMIN')
const roleLabel = computed(() => (isTechAdmin.value ? '技术管理员' : '管理员'))
import AiDrawer from '../components/AiDrawer.vue'

const aiVisible = ref(false)

const router = useRouter()
const handleCommand = async (cmd: string) => {
  if (cmd === 'logout') {
    try { await logout() } catch {}
    clearTokens()
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

/* AI 助手入口（侧边栏底部） */
.ai-entry {
  margin: 12px 14px;
  padding: 10px 0;
  width: calc(100% - 28px);
  border: 1px solid rgba(52, 211, 153, 0.35);
  border-radius: 10px;
  background: rgba(52, 211, 153, 0.08);
  color: #6ee7b7;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.ai-entry:hover {
  background: rgba(52, 211, 153, 0.18);
  box-shadow: 0 0 14px rgba(52, 211, 153, 0.25);
}

.ai-entry-orb {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 8px rgba(52, 211, 153, 0.8);
}
</style>
