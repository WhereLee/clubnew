<template>
  <router-view />
  <!-- AI 助手悬浮入口（全站） -->
  <button v-if="showFab" class="ai-fab" @click="aiVisible = true">
    <span class="ai-fab-orb"></span>
  </button>
  <AiDrawer :visible="aiVisible" @close="aiVisible = false" />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import AiDrawer from './components/AiDrawer.vue'

const route = useRoute()
const aiVisible = ref(false)
// 登录页不显示悬浮球
const showFab = computed(() => route.path !== '/login')
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 28px;
  bottom: 32px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #34d399, #059669);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.4);
  transition: all 0.25s;
  z-index: 1999;
}

.ai-fab:hover {
  transform: translateY(-3px) scale(1.04);
  box-shadow: 0 12px 30px rgba(16, 185, 129, 0.55);
}

.ai-fab-orb {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2.5px solid rgba(255, 255, 255, 0.9);
  position: relative;
}

.ai-fab-orb::after {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: #fff;
}
</style>

