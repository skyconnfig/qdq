import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { canAccessRoute, getDefaultRoute } from '@/config/roles'

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
      },
      // 主持人路由
      {
        path: 'host',
        meta: { title: '主持人', roles: ['HOST', 'SUPER_ADMIN'] },
        children: [
          {
            path: 'sessions',
            name: 'HostSessions',
            component: () => import('@/views/host/sessions.vue'),
            meta: { title: '我的比赛', roles: ['HOST', 'SUPER_ADMIN'] }
          },
          {
            path: 'control/:id',
            name: 'HostControl',
            component: () => import('@/views/host/control.vue'),
            meta: { title: '控制台', roles: ['HOST', 'SUPER_ADMIN'] }
          }
        ]
      },
      // 选手路由
      {
        path: 'player',
        meta: { title: '选手', roles: ['PLAYER', 'SUPER_ADMIN'] },
        children: [
          {
            path: 'sessions',
            name: 'PlayerSessions',
            component: () => import('@/views/player/sessions.vue'),
            meta: { title: '我的比赛', roles: ['PLAYER', 'SUPER_ADMIN'] }
          }
        ]
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
  
  // 检查角色权限
  const userRoles = userStore.userInfo?.roles || []
  
  // 如果访问根路径，跳转到默认页面
  if (to.path === '/' || to.path === '/dashboard') {
    const defaultRoute = getDefaultRoute(userRoles)
    if (defaultRoute !== '/dashboard') {
      next(defaultRoute)
      return
    }
  }
  
  // 检查是否有权限访问该路由
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const hasRole = to.meta.roles.some((role: string) => userRoles.includes(role))
    if (!hasRole) {
      // 没有权限，跳转到默认页面
      const defaultRoute = getDefaultRoute(userRoles)
      next(defaultRoute)
      return
    }
  }
  
  // 使用角色配置检查路由访问权限
  if (!canAccessRoute(userRoles, to.path)) {
    // 没有权限，跳转到默认页面
    const defaultRoute = getDefaultRoute(userRoles)
    next(defaultRoute)
    return
  }
  
  next()
})

export default router
