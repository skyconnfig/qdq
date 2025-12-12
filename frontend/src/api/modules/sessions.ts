import { http } from '../request'

/**
 * 比赛场次管理接口
 */

export interface Session {
  id?: number
  name: string
  description?: string
  mode: number  // 1-个人赛，2-团队赛
  questionIds: number[]
  scheduledStart?: string
  status?: number
  questionCount?: number
  createdAt?: string
}

export interface SessionQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number | null
}

// 分页查询场次
export function getSessionList(params: SessionQuery) {
  return http.get('/sessions', params)
}

// 获取场次详情
export function getSessionDetail(id: number) {
  return http.get(`/sessions/${id}`)
}

// 创建场次
export function createSession(data: Session) {
  return http.post('/sessions', data)
}

// 更新场次
export function updateSession(id: number, data: Session) {
  return http.put(`/sessions/${id}`, data)
}

// 删除场次
export function deleteSession(id: number) {
  return http.delete(`/sessions/${id}`)
}

// 开始比赛
export function startSession(id: number) {
  return http.post(`/sessions/${id}/start`)
}

// 暂停比赛
export function pauseSession(id: number) {
  return http.post(`/sessions/${id}/pause`)
}

// 恢复比赛
export function resumeSession(id: number) {
  return http.post(`/sessions/${id}/resume`)
}

// 结束比赛
export function finishSession(id: number) {
  return http.post(`/sessions/${id}/finish`)
}

// 下一题
export function nextQuestion(id: number) {
  return http.post(`/sessions/${id}/next-question`)
}

// 获取当前题目
export function getCurrentQuestion(id: number) {
  return http.get(`/sessions/${id}/current-question`)
}

// 处理抢答结果
export function processBuzz(id: number, data: { questionId: number; userId: number; isCorrect: boolean }) {
  return http.post(`/sessions/${id}/process-buzz`, data)
}

// 关闭抢答
export function closeBuzz(id: number, questionId: number) {
  return http.post(`/sessions/${id}/close-buzz/${questionId}`)
}

// 获取场次在线人数
export function getOnlineCount(id: number) {
  return http.get(`/sessions/${id}/online-count`)
}
