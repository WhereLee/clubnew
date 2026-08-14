<template>
  <div class="home">
    <nav class="topnav">
      <span class="brand">🎓 社团社区</span>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/clubs">社团广场</router-link>
        <router-link to="/activities">活动广场</router-link>
      </div>
    </nav>
    <div class="content">
      <div class="banner">
        <h2>欢迎来到社团社区</h2>
        <p>发现兴趣，结交朋友，参与活动</p>
      </div>
      <div class="main-grid">
        <div class="feed">
          <h3>最新动态</h3>
          <el-empty description="暂无动态" />
        </div>
        <div class="sidebar">
          <h3>🏆 活跃社团排行</h3>
          <el-empty description="暂无数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.topnav { background: #fff; padding: 0 24px; height: 60px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.brand { font-size: 18px; font-weight: 600; color: var(--primary); }
.nav-links { display: flex; gap: 20px; }
.nav-links a { text-decoration: none; color: var(--text); }
.nav-links a:hover { color: var(--primary); }
.banner { background: linear-gradient(135deg, #2E5BFF 0%, #7C3AED 100%); color: #fff; padding: 40px; border-radius: 12px; margin: 20px 0; }
.banner h2 { font-size: 24px; margin-bottom: 8px; }
.main-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-top: 20px; }
.feed, .sidebar { background: #fff; padding: 20px; border-radius: 12px; }
h3 { margin-bottom: 16px; font-size: 16px; }
</style>
