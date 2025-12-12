<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { TrophyOutline, PeopleOutline, LibraryOutline, PlayOutline } from '@vicons/ionicons5'
import { getSessionList, getUserList, getQuestionList } from '@/api/modules'
import { NTag } from 'naive-ui'

const router = useRouter()

// 统计数据
const stats = ref([
  { title: '总用户数', value: 0, icon: PeopleOutline, color: '#18A058', unit: '人' },
  { title: '题库总数', value: 0, icon: LibraryOutline, color: '#2080f0', unit: '题' },
  { title: '比赛场次', value: 0, icon: TrophyOutline, color: '#f0a020', unit: '场' },
  { title: '进行中比赛', value: 0, icon: PlayOutline, color: '#e55353', unit: '场' }
])

// 快捷操作
const quickActions = [
  { title: '创建比赛', description: '快速创建新的比赛场次', route: '/sessions?action=create', color: '#18A058' },
  { title: '添加题目', description: '向题库添加新题目', route: '/questions?action=create', color: '#2080f0' },
  { title: '用户管理', description: '管理系统用户', route: '/users', color: '#f0a020' }
]

// 最近比赛
const recentSessions = ref<any[]>([])

// 加载数据
const loadData = async () => {
  try {
    // 加载用户总数
    const usersRes = await getUserList({ page: 1, pageSize: 1 })
    stats.value[0].value = usersRes.data.total || 0
    
    // 加载题目总数
    const questionsRes = await getQuestionList({ page: 1, pageSize: 1 })
    stats.value[1].value = questionsRes.data.total || 0
    
    // 加载比赛场次
    const sessionsRes = await getSessionList({ page: 1, pageSize: 1 })
    stats.value[2].value = sessionsRes.data.total || 0
    
    // 加载进行中的比赛
    const activeSessionsRes = await getSessionList({ page: 1, pageSize: 1, status: 2 })
    stats.value[3].value = activeSessionsRes.data.total || 0
    
    // 加载最近比赛
    const recentRes = await getSessionList({ page: 1, pageSize: 5 })
    recentSessions.value = recentRes.data.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})

const getStatusTag = (status: number) => {
  const map: Record<number, { type: 'default' | 'info' | 'success' | 'warning' | 'error'; text: string }> = {
    0: { type: 'default', text: '草稿' },
    1: { type: 'info', text: '待开始' },
    2: { type: 'success', text: '进行中' },
    3: { type: 'warning', text: '暂停' },
    4: { type: 'error', text: '已结束' }
  }
  return map[status] || { type: 'default', text: '未知' }
}

const goTo = (route: string) => {
  router.push(route)
}
</script>

<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">工作台</h1>
    </div>
    
    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <n-grid-item v-for="stat in stats" :key="stat.title" span="4 m:2 l:1">
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">
                <n-number-animation :from="0" :to="stat.value" />
                <span class="stat-unit">{{ stat.unit }}</span>
              </div>
            </div>
            <div class="stat-icon" :style="{ backgroundColor: stat.color + '15', color: stat.color }">
              <n-icon :size="28">
                <component :is="stat.icon" />
              </n-icon>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
    
    <!-- 主要内容区 -->
    <n-grid :cols="3" :x-gap="16" :y-gap="16" class="mt-lg" responsive="screen" item-responsive>
      <!-- 快捷操作 -->
      <n-grid-item span="3 l:1">
        <n-card title="快捷操作">
          <div class="quick-actions">
            <div
              v-for="action in quickActions"
              :key="action.title"
              class="action-item"
              @click="goTo(action.route)"
            >
              <div class="action-dot" :style="{ backgroundColor: action.color }"></div>
              <div class="action-info">
                <div class="action-title">{{ action.title }}</div>
                <div class="action-desc">{{ action.description }}</div>
              </div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      
      <!-- 最近比赛 -->
      <n-grid-item span="3 l:2">
        <n-card title="最近比赛">
          <n-data-table
            :columns="[
              { title: '比赛名称', key: 'name', ellipsis: { tooltip: true } },
              { 
                title: '状态', 
                key: 'status', 
                width: 100,
                render: (row: any) => h(NTag, { type: getStatusTag(row.status).type, size: 'small' }, () => getStatusTag(row.status).text) 
              },
              { title: '题目数', key: 'questionCount', width: 100 },
              { title: '创建时间', key: 'createdAt', width: 180 }
            ]"
            :data="recentSessions"
            :bordered="false"
            size="small"
          />
          
          <template #action>
            <n-button text type="primary" @click="goTo('/sessions')">
              查看全部
            </n-button>
          </template>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    
    .stat-info {
      .stat-title {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: $text-primary;
        
        .stat-unit {
          font-size: 14px;
          font-weight: normal;
          color: $text-secondary;
          margin-left: 4px;
        }
      }
    }
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .quick-actions {
    .action-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: $border-radius;
      cursor: pointer;
      transition: background $transition-fast;
      
      &:hover {
        background: $bg-hover;
      }
      
      &:not(:last-child) {
        margin-bottom: 8px;
      }
      
      .action-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 12px;
      }
      
      .action-info {
        .action-title {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
        }
        
        .action-desc {
          font-size: 12px;
          color: $text-secondary;
          margin-top: 2px;
        }
      }
    }
  }
}
</style>
