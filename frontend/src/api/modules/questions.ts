import { http } from '../request'

/**
 * 题目管理接口
 */

export interface Question {
  id?: number
  type: number  // 1-单选，2-多选，3-判断，4-填空，5-主观
  title: string
  content?: string
  options?: { key: string; value: string }[]
  answer: string
  analysis?: string
  score: number
  difficulty: number  // 1-简单，2-中等，3-困难
  tags?: string[]
  status?: number  // 0-草稿，1-已发布，2-待审核
  createdAt?: string
}

export interface QuestionQuery {
  page?: number
  pageSize?: number
  keyword?: string
  type?: number | null
  difficulty?: number | null
  status?: number | null
}

// 分页查询题目
export function getQuestionList(params: QuestionQuery) {
  return http.get('/questions', params)
}

// 获取题目详情
export function getQuestionDetail(id: number) {
  return http.get(`/questions/${id}`)
}

// 创建题目
export function createQuestion(data: Question) {
  return http.post('/questions', data)
}

// 更新题目
export function updateQuestion(id: number, data: Question) {
  return http.put(`/questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number) {
  return http.delete(`/questions/${id}`)
}

// 批量删除题目
export function batchDeleteQuestions(ids: number[]) {
  return http.delete('/questions/batch', { ids })
}

// 更新题目状态
export function updateQuestionStatus(id: number, status: number) {
  return http.post(`/questions/${id}/status`, { status })
}

// 获取随机题目
export function getRandomQuestion(params: { count?: number; difficulty?: number; type?: number }) {
  return http.get('/questions/random', params)
}
