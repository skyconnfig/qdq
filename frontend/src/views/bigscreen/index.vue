<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { wsService } from '@/services/websocket'

const route = useRoute()
const sessionId = Number(route.params.id)

const sessionInfo = ref<any>(null)
const currentQuestion = ref<any>(null)
const buzzResults = ref<any[]>([])
const scores = ref<any[]>([])
const countdown = ref(0)
const status = ref('waiting') // waiting, question, buzz_result, answer

// 初始化WebSocket连接
onMounted(async () => {
  try {
    await wsService.connect()
    wsService.joinSession(sessionId)
    
    // 监听事件
    wsService.on('session_state', (data) => {
      sessionInfo.value = data.session
      status.value = data.status
    })
    
    wsService.on('question_push', (data) => {
      currentQuestion.value = data
      status.value = 'question'
      buzzResults.value = []
    })
    
    wsService.on('buzz_result', (data) => {
      buzzResults.value = data.results
      status.value = 'buzz_result'
    })
    
    wsService.on('score_update', (data) => {
      scores.value = data.scores || []
    })
    
    wsService.on('countdown', (data) => {
      countdown.value = data.seconds
    })
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
})

onUnmounted(() => {
  wsService.leaveSession(sessionId)
})
</script>

<template>
  <div class="bigscreen">
    <div class="bigscreen-header">
      <h1 class="title">{{ sessionInfo?.name || '知识竞赛' }}</h1>
      <div class="status-bar">
        <span v-if="countdown > 0" class="countdown">{{ countdown }}s</span>
      </div>
    </div>
    
    <div class="bigscreen-content">
      <!-- 等待状态 -->
      <div v-if="status === 'waiting'" class="waiting-panel">
        <div class="waiting-icon">🎯</div>
        <h2>等待比赛开始...</h2>
      </div>
      
      <!-- 题目展示 -->
      <div v-else-if="status === 'question'" class="question-panel">
        <div class="question-type">
          {{ ['', '单选题', '多选题', '判断题', '填空题', '主观题'][currentQuestion?.type] || '题目' }}
        </div>
        <div class="question-title">{{ currentQuestion?.title }}</div>
        <div v-if="currentQuestion?.options" class="options-list">
          <div v-for="opt in currentQuestion.options" :key="opt.key" class="option-item">
            <span class="option-key">{{ opt.key }}</span>
            <span class="option-value">{{ opt.value }}</span>
          </div>
        </div>
      </div>
      
      <!-- 抢答结果 -->
      <div v-else-if="status === 'buzz_result'" class="buzz-panel">
        <h2>抢答结果</h2>
        <div class="buzz-list">
          <div v-for="(item, index) in buzzResults" :key="index" 
               class="buzz-item" :class="{ 'first': index === 0 }">
            <span class="rank">{{ index + 1 }}</span>
            <span class="name">{{ item.memberId }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 排行榜 -->
    <div class="scoreboard">
      <h3>排行榜</h3>
      <div class="score-list">
        <div v-for="(item, index) in scores.slice(0, 10)" :key="index" class="score-item">
          <span class="rank">{{ index + 1 }}</span>
          <span class="name">{{ item.name }}</span>
          <span class="score">{{ item.score }}分</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.bigscreen {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
}

.bigscreen-header {
  text-align: center;
  margin-bottom: 40px;
  
  .title {
    font-size: 48px;
    font-weight: 700;
    background: linear-gradient(90deg, #18A058, #36ad6a);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 16px;
  }
  
  .countdown {
    font-size: 72px;
    font-weight: 700;
    color: #f0a020;
  }
}

.bigscreen-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.waiting-panel {
  text-align: center;
  
  .waiting-icon {
    font-size: 120px;
    margin-bottom: 24px;
  }
  
  h2 {
    font-size: 32px;
    opacity: 0.8;
  }
}

.question-panel {
  max-width: 1200px;
  width: 100%;
  
  .question-type {
    display: inline-block;
    background: #18A058;
    padding: 8px 24px;
    border-radius: 20px;
    margin-bottom: 24px;
    font-size: 18px;
  }
  
  .question-title {
    font-size: 36px;
    line-height: 1.6;
    margin-bottom: 40px;
  }
  
  .options-list {
    .option-item {
      display: flex;
      align-items: center;
      background: rgba(255, 255, 255, 0.1);
      padding: 20px 32px;
      border-radius: 12px;
      margin-bottom: 16px;
      font-size: 24px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.15);
      }
      
      .option-key {
        width: 48px;
        height: 48px;
        background: #18A058;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        margin-right: 24px;
      }
    }
  }
}

.buzz-panel {
  text-align: center;
  
  h2 {
    font-size: 36px;
    margin-bottom: 32px;
  }
  
  .buzz-list {
    .buzz-item {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 24px;
      padding: 16px 48px;
      font-size: 28px;
      margin-bottom: 16px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      
      &.first {
        background: linear-gradient(90deg, #18A058, #36ad6a);
        font-size: 36px;
        animation: pulse 1s infinite;
      }
      
      .rank {
        font-weight: 700;
      }
    }
  }
}

.scoreboard {
  position: fixed;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.1);
  padding: 24px;
  border-radius: 16px;
  min-width: 200px;
  
  h3 {
    text-align: center;
    margin-bottom: 16px;
    font-size: 20px;
  }
  
  .score-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    .rank {
      width: 24px;
      font-weight: 600;
      color: #f0a020;
    }
    
    .name {
      flex: 1;
    }
    
    .score {
      color: #18A058;
      font-weight: 600;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}
</style>
