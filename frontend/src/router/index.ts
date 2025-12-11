import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'HomeOutline' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/index.vue'),
        meta: { title: '用户管理', icon: 'PeopleOutline' }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/questions/index.vue'),
        meta: { title: '题库管理', icon: 'LibraryOutline' }
      },
      {
        path: 'sessions',
        name: 'Sessions',
        component: () => import('@/views/sessions/index.vue'),
        meta: { title: '比赛管理', icon: 'TrophyOutline' }
      },
      {
        path: 'sessions/:id',
        name: 'SessionDetail',
        component: () => import('@/views/sessions/detail.vue'),
        meta: { title: '比赛详情', hidden: true }
      },
      {
        path: 'sessions/:id/control',
        name: 'SessionControl',
        component: () => import('@/views/sessions/control.vue'),
        meta: { title: '主持人控制台', hidden: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '系统设置', icon: 'SettingsOutline' }
      }
    ]
  },
  {
    path: '/bigscreen/:id',
    name: 'BigScreen',
    component: () => import('@/views/bigscreen/index.vue'),
    meta: { title: '大屏展示', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '知识竞赛抢答系统'} - 知识竞赛抢答系统`
  
  // 不需要认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  const userStore = useUserStore()
  
  // 检查是否登录
  if (!userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 如果没有用户信息，尝试获取
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch {
      userStore.logout()
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  
  next()
})

export default router
