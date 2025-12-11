import { defineStore } from 'pinia'
import { http } from '@/api/request'

interface UserInfo {
  userId: number
  username: string
  name: string
  avatar: string
  roles: string[]
  permissions: string[]
}

interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

interface LoginResponse {
  data: UserInfo & { token: string }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.roles?.includes('SUPER_ADMIN') || false,
    isHost: (state) => state.userInfo?.roles?.includes('HOST') || false,
    isJudge: (state) => state.userInfo?.roles?.includes('JUDGE') || false,
    hasPermission: (state) => (permission: string) => {
      return state.userInfo?.permissions?.includes(permission) || false
    }
  },
  
  actions: {
    async login(data: LoginRequest) {
      const res = await http.post<LoginResponse>('/auth/login', data)
      const { token, ...userInfo } = res.data
      
      this.token = token
      this.userInfo = userInfo
      
      localStorage.setItem('token', token)
      
      return res
    },
    
    async getUserInfo() {
      const res = await http.get<{ data: UserInfo }>('/auth/me')
      this.userInfo = res.data
      return res.data
    },
    
    async logout() {
      try {
        await http.post('/auth/logout')
      } catch {
        // 忽略登出错误
      }
      
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    },
    
    async changePassword(oldPassword: string, newPassword: string) {
      await http.post('/auth/change-password', null, {
        params: { oldPassword, newPassword }
      })
      await this.logout()
    }
  }
})
