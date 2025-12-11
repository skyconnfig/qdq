<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TrophyOutline, PeopleOutline, LibraryOutline, PlayOutline } from '@vicons/ionicons5'

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
onMounted(async () => {
  // TODO: 从API加载真实数据
  stats.value[0].value = 128
  stats.value[1].value = 560
  stats.value[2].value = 45
  stats.value[3].value = 2
  
  recentSessions.value = [
    { id: 1, name: '知识竞赛第一轮', status: 2, participantCount: 24, createdAt: '2024-01-15' },
    { id: 2, name: '企业文化知识竞赛', status: 4, participantCount: 36, createdAt: '2024-01-10' },
    { id: 3, name: '安全知识问答', status: 0, participantCount: 0, createdAt: '2024-01-08' }
  ]
})

const getStatusTag = (status: number) => {
  const map: Record<number, { type: string; text: string }> = {
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
              { title: '比赛名称', key: 'name' },
              { title: '状态', key: 'status', render: (row: any) => h('n-tag', { type: getStatusTag(row.status).type, size: 'small' }, () => getStatusTag(row.status).text) },
              { title: '参与人数', key: 'participantCount' },
              { title: '创建时间', key: 'createdAt' }
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

<script lang="ts">
import { h } from 'vue'
</script>

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
