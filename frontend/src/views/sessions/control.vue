<script setup lang="ts">
import { ref, onMounted, onUnmounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog, NButton, NTag } from 'naive-ui'
import {
  getSessionDetail,
  getCurrentQuestion,
  startSession,
  pauseSession,
  resumeSession,
  finishSession,
  nextQuestion,
  processBuzz,
  closeBuzz,
  getOnlineCount
} from '@/api/modules'
import {
  ArrowBackOutline,
  PlayOutline,
  PauseOutline,
  PlaySkipForwardOutline,
  StopOutline,
  CloseCircleOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const sessionId = Number(route.params.id)
const loading = ref(false)
const sessionData = ref<any>({
  name: '',
  status: 0,
  mode: 1
})

const currentQuestion = ref<any>(null)
const buzzRecords = ref<any[]>([])
const onlineCount = ref(0)

// WebSocket 连接
let ws: WebSocket | null = null

const statusMap: Record<number, { type: any; text: string }> = {
  0: { type: 'default', text: '草稿' },
  1: { type: 'info', text: '待开始' },
  2: { type: 'success', text: '进行中' },
  3: { type: 'warning', text: '暂停' },
  4: { type: 'error', text: '已结束' }
}

// 加载比赛信息
const loadSessionData = async () => {
  try {
    const res = await getSessionDetail(sessionId)
    sessionData.value = res.data
  } catch (error: any) {
    message.error(error.message)
  }
}

// 加载当前题目
const loadCurrentQuestion = async () => {
  try {
    const res = await getCurrentQuestion(sessionId)
    currentQuestion.value = res.data
  } catch (error: any) {
    console.error(error)
  }
}

// 加载在线人数
const loadOnlineCount = async () => {
  try {
    const res = await getOnlineCount(sessionId)
    onlineCount.value = res.data || 0
  } catch (error: any) {
    console.error(error)
  }
}

// 开始比赛
const handleStart = async () => {
  try {
    loading.value = true
    await startSession(sessionId)
    message.success('比赛已开始')
    await loadSessionData()
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 暂停比赛
const handlePause = async () => {
  try {
    loading.value = true
    await pauseSession(sessionId)
    message.success('比赛已暂停')
    await loadSessionData()
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 恢复比赛
const handleResume = async () => {
  try {
    loading.value = true
    await resumeSession(sessionId)
    message.success('比赛已恢复')
    await loadSessionData()
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 结束比赛
const handleFinish = () => {
  dialog.warning({
    title: '结束确认',
    content: '确定要结束比赛吗？结束后不可恢复！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        loading.value = true
        await finishSession(sessionId)
        message.success('比赛已结束')
        await loadSessionData()
      } catch (error: any) {
        message.error(error.message)
      } finally {
        loading.value = false
      }
    }
  })
}

// 下一题
const handleNextQuestion = async () => {
  try {
    loading.value = true
    await nextQuestion(sessionId)
    message.success('已推送下一题')
    await loadCurrentQuestion()
    buzzRecords.value = []
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 关闭抢答
const handleCloseBuzz = async () => {
  if (!currentQuestion.value?.id) {
    message.warning('当前无题目')
    return
  }
  
  try {
    await closeBuzz(sessionId, currentQuestion.value.id)
    message.success('已关闭抢答')
  } catch (error: any) {
    message.error(error.message)
  }
}

// 处理抢答结果
const handleProcessBuzz = async (record: any, isCorrect: boolean) => {
  try {
    await processBuzz(sessionId, {
      questionId: currentQuestion.value.id,
      userId: record.userId,
      isCorrect
    })
    message.success(isCorrect ? '已标记为正确' : '已标记为错误')
    record.result = isCorrect ? 'correct' : 'wrong'
  } catch (error: any) {
    message.error(error.message)
  }
}

// WebSocket 初始化
const initWebSocket = () => {
  const wsUrl = `ws://localhost:8080/ws/session/${sessionId}`
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket 连接成功')
  }
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    
    // 处理不同类型的消息
    switch (data.type) {
      case 'BUZZ':
        // 抢答记录
        buzzRecords.value.push({
          userId: data.userId,
          username: data.username,
          timestamp: data.timestamp,
          result: null
        })
        break
      case 'QUESTION_CHANGE':
        // 题目变化
        loadCurrentQuestion()
        break
      case 'SESSION_STATUS':
        // 比赛状态变化
        loadSessionData()
        break
      case 'ONLINE_COUNT':
        // 在线人数更新
        onlineCount.value = data.count
        break
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket 错误:', error)
  }
  
  ws.onclose = () => {
    console.log('WebSocket 连接关闭')
  }
}

onMounted(() => {
  loadSessionData()
  loadCurrentQuestion()
  loadOnlineCount()
  initWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<template>
  <div class="session-control">
    <div class="page-header">
      <n-space align="center">
        <n-button text @click="router.back()">
          <template #icon><n-icon><ArrowBackOutline /></n-icon></template>
          返回
        </n-button>
        <h1 class="page-title">控制台 - {{ sessionData.name }}</h1>
      </n-space>
      
      <n-space>
        <n-tag :type="statusMap[sessionData.status]?.type" size="large">
          {{ statusMap[sessionData.status]?.text }}
        </n-tag>
        <n-statistic label="在线人数" :value="onlineCount" />
      </n-space>
    </div>
    
    <n-grid :cols="3" :x-gap="16" :y-gap="16">
      <!-- 左侧：比赛控制 -->
      <n-grid-item :span="1">
        <n-card title="比赛控制">
          <n-space vertical size="large">
            <n-button
              v-if="sessionData.status === 0 || sessionData.status === 1"
              type="success"
              size="large"
              block
              :loading="loading"
              @click="handleStart"
            >
              <template #icon><n-icon><PlayOutline /></n-icon></template>
              开始比赛
            </n-button>
            
            <n-button
              v-if="sessionData.status === 2"
              type="warning"
              size="large"
              block
              :loading="loading"
              @click="handlePause"
            >
              <template #icon><n-icon><PauseOutline /></n-icon></template>
              暂停比赛
            </n-button>
            
            <n-button
              v-if="sessionData.status === 3"
              type="success"
              size="large"
              block
              :loading="loading"
              @click="handleResume"
            >
              <template #icon><n-icon><PlayOutline /></n-icon></template>
              恢复比赛
            </n-button>
            
            <n-button
              v-if="sessionData.status === 2"
              type="primary"
              size="large"
              block
              :loading="loading"
              @click="handleNextQuestion"
            >
              <template #icon><n-icon><PlaySkipForwardOutline /></n-icon></template>
              下一题
            </n-button>
            
            <n-button
              v-if="sessionData.status === 2"
              type="error"
              size="large"
              block
              ghost
              @click="handleCloseBuzz"
            >
              <template #icon><n-icon><CloseCircleOutline /></n-icon></template>
              关闭抢答
            </n-button>
            
            <n-button
              v-if="sessionData.status === 2 || sessionData.status === 3"
              type="error"
              size="large"
              block
              @click="handleFinish"
            >
              <template #icon><n-icon><StopOutline /></n-icon></template>
              结束比赛
            </n-button>
          </n-space>
        </n-card>
      </n-grid-item>
      
      <!-- 中间：当前题目 -->
      <n-grid-item :span="1">
        <n-card title="当前题目">
          <div v-if="currentQuestion" class="question-content">
            <n-space vertical size="large">
              <div>
                <n-tag type="info" size="small">
                  {{
                    ({
                      1: '单选题',
                      2: '多选题',
                      3: '判断题',
                      4: '填空题',
                      5: '主观题'
                    } as Record<number, string>)[currentQuestion.type] || '-'
                  }}
                </n-tag>
                <n-tag
                  :type="
                    ({
                      1: 'success',
                      2: 'warning',
                      3: 'error'
                    } as Record<number, 'success' | 'warning' | 'error'>)[currentQuestion.difficulty] || 'info'
                  "
                  size="small"
                  style="margin-left: 8px"
                >
                  {{
                    ({
                      1: '简单',
                      2: '中等',
                      3: '困难'
                    } as Record<number, string>)[currentQuestion.difficulty] || '-'
                  }}
                </n-tag>
              </div>
              
              <div class="question-title">{{ currentQuestion.title }}</div>
              
              <div v-if="currentQuestion.options" class="question-options">
                <div
                  v-for="opt in currentQuestion.options"
                  :key="opt.key"
                  class="option-item"
                >
                  <span class="option-key">{{ opt.key }}.</span>
                  <span>{{ opt.value }}</span>
                </div>
              </div>
              
              <n-alert type="success" title="正确答案">
                {{ currentQuestion.answer }}
              </n-alert>
              
              <n-alert v-if="currentQuestion.analysis" type="info" title="答案解析">
                {{ currentQuestion.analysis }}
              </n-alert>
            </n-space>
          </div>
          <n-empty v-else description="暂无题目" />
        </n-card>
      </n-grid-item>
      
      <!-- 右侧：抢答记录 -->
      <n-grid-item :span="1">
        <n-card title="抢答记录">
          <n-list v-if="buzzRecords.length > 0">
            <n-list-item v-for="(record, index) in buzzRecords" :key="index">
              <n-space justify="space-between" style="width: 100%">
                <n-space>
                  <n-tag type="info" size="small">#{{ index + 1 }}</n-tag>
                  <span>{{ record.username }}</span>
                  <n-tag
                    v-if="record.result"
                    :type="record.result === 'correct' ? 'success' : 'error'"
                    size="small"
                  >
                    {{ record.result === 'correct' ? '正确' : '错误' }}
                  </n-tag>
                </n-space>
                <n-space v-if="!record.result">
                  <n-button
                    size="small"
                    type="success"
                    @click="handleProcessBuzz(record, true)"
                  >
                    正确
                  </n-button>
                  <n-button
                    size="small"
                    type="error"
                    @click="handleProcessBuzz(record, false)"
                  >
                    错误
                  </n-button>
                </n-space>
              </n-space>
            </n-list-item>
          </n-list>
          <n-empty v-else description="暂无抢答记录" />
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<style lang="scss" scoped>
.session-control {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  
  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
  
  .question-content {
    .question-title {
      font-size: 18px;
      font-weight: 500;
      line-height: 1.6;
    }
    
    .question-options {
      .option-item {
        padding: 8px 0;
        
        .option-key {
          font-weight: 600;
          margin-right: 8px;
          color: $primary;
        }
      }
    }
  }
}
</style>
