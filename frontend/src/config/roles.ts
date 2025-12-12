/**
 * 角色权限配置
 */

export enum Role {
  SUPER_ADMIN = 'SUPER_ADMIN',
  HOST = 'HOST',
  JUDGE = 'JUDGE',
  PLAYER = 'PLAYER',
  VIEWER = 'VIEWER'
}

export interface RoleConfig {
  code: Role
  name: string
  description: string
  permissions: string[]
  routes: string[]
  defaultRoute: string
}

// 角色配置
export const ROLE_CONFIG: Record<Role, RoleConfig> = {
  [Role.SUPER_ADMIN]: {
    code: Role.SUPER_ADMIN,
    name: '超级管理员',
    description: '系统超级管理员，拥有所有权限',
    permissions: ['*'],
    routes: [
      '/dashboard',
      '/users',
      '/questions',
      '/sessions',
      '/settings'
    ],
    defaultRoute: '/dashboard'
  },
  
  [Role.HOST]: {
    code: Role.HOST,
    name: '主持人',
    description: '比赛主持人，控制比赛流程',
    permissions: [
      'session:view',
      'session:control',
      'question:view',
      'buzz:manage'
    ],
    routes: [
      '/host/sessions',
      '/host/control',
      '/settings'
    ],
    defaultRoute: '/host/sessions'
  },
  
  [Role.JUDGE]: {
    code: Role.JUDGE,
    name: '评委',
    description: '比赛评委，评判主观题',
    permissions: [
      'session:view',
      'question:judge',
      'answer:review'
    ],
    routes: [
      '/judge/sessions',
      '/judge/review',
      '/settings'
    ],
    defaultRoute: '/judge/sessions'
  },
  
  [Role.PLAYER]: {
    code: Role.PLAYER,
    name: '选手',
    description: '参赛选手',
    permissions: [
      'session:join',
      'question:answer',
      'buzz:submit'
    ],
    routes: [
      '/player/sessions',
      '/player/compete',
      '/player/results',
      '/settings'
    ],
    defaultRoute: '/player/sessions'
  },
  
  [Role.VIEWER]: {
    code: Role.VIEWER,
    name: '观众',
    description: '观看比赛',
    permissions: [
      'session:view'
    ],
    routes: [
      '/viewer/sessions',
      '/viewer/watch'
    ],
    defaultRoute: '/viewer/sessions'
  }
}

/**
 * 检查用户是否有指定权限
 */
export function hasPermission(userRoles: string[], permission: string): boolean {
  if (!userRoles || userRoles.length === 0) return false
  
  // 超级管理员拥有所有权限
  if (userRoles.includes(Role.SUPER_ADMIN)) return true
  
  // 检查每个角色的权限
  return userRoles.some(roleCode => {
    const role = ROLE_CONFIG[roleCode as Role]
    if (!role) return false
    
    // 检查是否有通配符权限
    if (role.permissions.includes('*')) return true
    
    // 检查是否有指定权限
    return role.permissions.includes(permission)
  })
}

/**
 * 检查用户是否可以访问指定路由
 */
export function canAccessRoute(userRoles: string[], routePath: string): boolean {
  if (!userRoles || userRoles.length === 0) return false
  
  // 超级管理员可以访问所有路由
  if (userRoles.includes(Role.SUPER_ADMIN)) return true
  
  // 检查每个角色是否可以访问该路由
  return userRoles.some(roleCode => {
    const role = ROLE_CONFIG[roleCode as Role]
    if (!role) return false
    
    // 检查路由是否在允许列表中
    return role.routes.some(allowedRoute => {
      return routePath === allowedRoute || routePath.startsWith(allowedRoute + '/')
    })
  })
}

/**
 * 获取用户的默认路由
 */
export function getDefaultRoute(userRoles: string[]): string {
  if (!userRoles || userRoles.length === 0) return '/login'
  
  // 优先使用第一个角色的默认路由
  const firstRole = ROLE_CONFIG[userRoles[0] as Role]
  return firstRole?.defaultRoute || '/dashboard'
}
