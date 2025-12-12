import { http } from '../request'

/**
 * 认证相关接口
 */

// 登录
export function login(data: { username: string; password: string; rememberMe?: boolean }) {
  return http.post('/auth/login', data)
}

// 登出
export function logout() {
  return http.post('/auth/logout')
}

// 获取当前用户信息
export function getCurrentUser() {
  return http.get('/auth/me')
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return http.post('/auth/change-password', data)
}
