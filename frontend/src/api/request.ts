import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    
    // 添加 token
    if (userStore.token) {
      config.headers.Authorization = userStore.token
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 成功响应
    if (res.code === 0) {
      return res
    }
    
    // 未登录或token过期
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push({ name: 'Login' })
      return Promise.reject(new Error(res.message || '请先登录'))
    }
    
    // 无权限
    if (res.code === 403) {
      return Promise.reject(new Error(res.message || '无权限执行此操作'))
    }
    
    // 其他错误
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '请先登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          message = '无权限访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    }
    
    return Promise.reject(new Error(message))
  }
)

export default request

// 封装常用请求方法
export const http = {
  get<T = any>(url: string, params?: object): Promise<T> {
    return request.get(url, { params })
  },
  
  post<T = any>(url: string, data?: object): Promise<T> {
    return request.post(url, data)
  },
  
  put<T = any>(url: string, data?: object): Promise<T> {
    return request.put(url, data)
  },
  
  delete<T = any>(url: string, params?: object): Promise<T> {
    return request.delete(url, { params })
  }
}
