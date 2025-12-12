import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '@/api/modules'

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
    isPlayer: (state) => state.userInfo?.roles?.includes('PLAYER') || false,
    isViewer: (state) => state.userInfo?.roles?.includes('VIEWER') || false,
    hasPermission: (state) => (permission: string) => {
      return state.userInfo?.permissions?.includes(permission) || false
    },
    // 获取用户的主要角色
    primaryRole: (state) => {
      const roles = state.userInfo?.roles || []
      if (roles.includes('SUPER_ADMIN')) return 'SUPER_ADMIN'
      if (roles.includes('HOST')) return 'HOST'
      if (roles.includes('JUDGE')) return 'JUDGE'
      if (roles.includes('PLAYER')) return 'PLAYER'
      if (roles.includes('VIEWER')) return 'VIEWER'
      return null
    }
  },
  
  actions: {
    async login(data: LoginRequest) {
      const res = await apiLogin(data)
      const { token, ...userInfo } = res.data
      
      this.token = token
      this.userInfo = userInfo
      
      localStorage.setItem('token', token)
      
      return res
    },
    
    async getUserInfo() {
      const res = await getCurrentUser()
      this.userInfo = res.data
      return res.data
    },
    
    async logout() {
      try {
        await apiLogout()
      } catch {
        // 忽略登出错误
      }
      
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
