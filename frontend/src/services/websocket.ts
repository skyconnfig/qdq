/**
 * WebSocket 服务
 * 支持自动重连、心跳检测
 */
export class WebSocketService {
  private ws: WebSocket | null = null
  private url: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private heartbeatInterval = 30000
  private heartbeatTimer: number | null = null
  private reconnectTimer: number | null = null
  private listeners: Map<string, Set<(data: any) => void>> = new Map()
  private isConnecting = false

  constructor(url: string) {
    this.url = url
  }

  /**
   * 连接WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        resolve()
        return
      }

      if (this.isConnecting) {
        reject(new Error('正在连接中'))
        return
      }

      this.isConnecting = true

      try {
        this.ws = new WebSocket(this.url)

        this.ws.onopen = () => {
          console.log('[WebSocket] 连接成功')
          this.isConnecting = false
          this.reconnectAttempts = 0
          this.startHeartbeat()
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data)
            this.handleMessage(message)
          } catch (e) {
            console.error('[WebSocket] 消息解析失败:', e)
          }
        }

        this.ws.onclose = (event) => {
          console.log('[WebSocket] 连接关闭:', event.code, event.reason)
          this.isConnecting = false
          this.stopHeartbeat()
          this.attemptReconnect()
        }

        this.ws.onerror = (error) => {
          console.error('[WebSocket] 连接错误:', error)
          this.isConnecting = false
          reject(error)
        }
      } catch (error) {
        this.isConnecting = false
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()
    this.stopReconnect()
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 发送消息
   */
  send(event: string, data: any = {}) {
    if (this.ws?.readyState !== WebSocket.OPEN) {
      console.warn('[WebSocket] 连接未就绪，无法发送消息')
      return false
    }

    const message = JSON.stringify({
      event,
      data,
      timestamp: Date.now()
    })

    this.ws.send(message)
    return true
  }

  /**
   * 订阅事件
   */
  on(event: string, callback: (data: any) => void) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event)!.add(callback)

    // 返回取消订阅函数
    return () => {
      this.listeners.get(event)?.delete(callback)
    }
  }

  /**
   * 取消订阅
   */
  off(event: string, callback?: (data: any) => void) {
    if (callback) {
      this.listeners.get(event)?.delete(callback)
    } else {
      this.listeners.delete(event)
    }
  }

  /**
   * 加入场次
   */
  joinSession(sessionId: number, teamId?: number, token?: string) {
    return this.send('join_session', { sessionId, teamId, token })
  }

  /**
   * 离开场次
   */
  leaveSession(sessionId: number) {
    return this.send('leave_session', { sessionId })
  }

  /**
   * 抢答
   */
  buzz(sessionId: number, questionId: number, userId?: number, teamId?: number) {
    return this.send('client_buzz', { sessionId, questionId, userId, teamId })
  }

  /**
   * 提交答案
   */
  submitAnswer(sessionId: number, questionId: number, answer: any) {
    return this.send('submit_answer', { sessionId, questionId, answer })
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(message: { event: string; data: any; timestamp: number }) {
    const { event, data } = message

    // 处理pong
    if (event === 'pong') {
      return
    }

    // 触发事件监听器
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      callbacks.forEach(callback => callback(data))
    }

    // 触发通配符监听器
    const wildcardCallbacks = this.listeners.get('*')
    if (wildcardCallbacks) {
      wildcardCallbacks.forEach(callback => callback({ event, data }))
    }
  }

  /**
   * 开始心跳
   */
  private startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      this.send('ping')
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 尝试重连
   */
  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[WebSocket] 达到最大重连次数，停止重连')
      return
    }

    this.reconnectAttempts++
    console.log(`[WebSocket] ${this.reconnectInterval / 1000}秒后尝试第${this.reconnectAttempts}次重连...`)

    this.reconnectTimer = window.setTimeout(() => {
      this.connect().catch(() => {
        // 重连失败，会在onclose中继续尝试
      })
    }, this.reconnectInterval)
  }

  /**
   * 停止重连
   */
  private stopReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.reconnectAttempts = 0
  }

  /**
   * 获取连接状态
   */
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

// 创建默认实例
const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/quiz`
export const wsService = new WebSocketService(wsUrl)

export default wsService
