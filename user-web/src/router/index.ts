import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/clubs', name: 'Clubs', component: () => import('../views/Clubs.vue') },
  { path: '/club/:id', name: 'ClubDetail', component: () => import('../views/ClubDetail.vue') },
  { path: '/club/create', name: 'ClubCreate', component: () => import('../views/ClubCreate.vue') },
  { path: '/my/club', name: 'MyClub', component: () => import('../views/MyClub.vue') },
  { path: '/recruits', name: 'Recruits', component: () => import('../views/Recruits.vue') },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue') },
  { path: '/activities', name: 'Activities', component: () => import('../views/Activities.vue') },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register') return next()
  if (!token) return next('/login')
  next()
})

export default router
