<template>
  <div>
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <router-link to="/">返回首页</router-link>
    </nav>
    <div class="content">
      <h2>活动广场</h2>
      <el-empty description="暂无活动" />
    </div>
  </div>
</template>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.brand { font-size: 18px; font-weight: 600; color: var(--primary); }
.content { padding: 20px; }
</style>
