import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'system/user', name: 'User', component: () => import('../views/system/User.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'Role', component: () => import('../views/system/Role.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'Menu', component: () => import('../views/system/Menu.vue'), meta: { title: '菜单管理' } },
      { path: 'system/dict', name: 'Dict', component: () => import('../views/system/Dict.vue'), meta: { title: '字典管理' } },
      { path: 'system/config', name: 'Config', component: () => import('../views/system/Config.vue'), meta: { title: '参数配置' } },
      { path: 'club/list', name: 'ClubList', component: () => import('../views/club/ClubList.vue'), meta: { title: '社团管理' } },
      { path: 'club/audit', name: 'ClubAudit', component: () => import('../views/club/ClubAudit.vue'), meta: { title: '社团审批' } },
      { path: 'recruit/list', name: 'Recruit', component: () => import('../views/recruit/Recruit.vue'), meta: { title: '纳新管理' } },
      { path: 'activity/list', name: 'Activity', component: () => import('../views/activity/Activity.vue'), meta: { title: '活动管理' } },
      { path: 'fund/audit', name: 'FundAudit', component: () => import('../views/fund/FundAudit.vue'), meta: { title: '经费审批' } },
      { path: 'rank', name: 'Rank', component: () => import('../views/rank/Rank.vue'), meta: { title: '排行榜' } },
      { path: 'log/oper', name: 'OperLog', component: () => import('../views/log/OperLog.vue'), meta: { title: '操作日志' } },
      { path: 'log/login', name: 'LoginLog', component: () => import('../views/log/LoginLog.vue'), meta: { title: '登录日志' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') return next()
  if (!token) return next('/login')
  next()
})

export default router
