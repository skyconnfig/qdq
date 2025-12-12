import { http } from '../request'

/**
 * 用户管理接口
 */

export interface User {
  id?: number
  username: string
  password?: string
  name: string
  phone?: string
  email?: string
  avatar?: string
  status?: number
  roleIds?: number[]
  roles?: string[]
  createdAt?: string
}

export interface UserQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number | null
}

// 分页查询用户
export function getUserList(params: UserQuery) {
  return http.get('/users', params)
}

// 根据ID查询用户
export function getUserById(id: number) {
  return http.get(`/users/${id}`)
}

// 创建用户
export function createUser(data: User) {
  return http.post('/users', data)
}

// 更新用户
export function updateUser(id: number, data: User) {
  return http.put(`/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number) {
  return http.delete(`/users/${id}`)
}

// 批量删除用户
export function batchDeleteUsers(ids: number[]) {
  return http.delete('/users/batch', { ids })
}

// 重置用户密码
export function resetUserPassword(id: number, newPassword: string) {
  return http.post(`/users/${id}/reset-password`, { newPassword })
}

// 更新用户状态
export function updateUserStatus(id: number, status: number) {
  return http.post(`/users/${id}/status`, { status })
}
