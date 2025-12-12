<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NTag, type DataTableColumns } from 'naive-ui'
import { getSessionList } from '@/api/modules'
import { PlayCircleOutline, TrophyOutline, RefreshOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref<any[]>([])

const columns: DataTableColumns<any> = [
  { title: '比赛名称', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '模式',
    key: 'mode',
    width: 100,
    render: (row: any) => row.mode === 1 ? '个人赛' : '团队赛'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const statusMap: Record<number, { type: 'default' | 'info' | 'success' | 'warning' | 'error'; text: string }> = {
        1: { type: 'info', text: '待开始' },
        2: { type: 'success', text: '进行中' },
        3: { type: 'warning', text: '暂停' }
      }
      const status = statusMap[row.status]
      return status ? h(NTag, { type: status.type, size: 'small' }, () => status.text) : null
    }
  },
  { title: '题目数', key: 'questionCount', width: 100 },
  { title: '计划开始时间', key: 'scheduledStart', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right' as const,
    render: (row: any) => row.status === 2 ? h(NButton, {
      size: 'small',
      type: 'primary',
      onClick: () => router.push(`/player/compete/${row.id}`)
    }, () => h('span', { style: 'display: flex; align-items: center; gap: 4px;' }, [
      h(PlayCircleOutline),
      '进入比赛'
    ])) : h(NButton, {
      size: 'small',
      disabled: true
    }, () => '未开始')
  }
]

const loadData = async () => {
  loading.value = true
  try {
    // 只显示待开始和进行中的比赛
    const res = await getSessionList({ page: 1, pageSize: 100 })
    data.value = (res.data.records || []).filter((s: any) => [1, 2, 3].includes(s.status))
  } catch (error: any) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="player-sessions">
    <div class="page-header">
      <div>
        <h1 class="page-title">
          <n-icon size="28" color="#18A058" style="vertical-align: middle; margin-right: 8px;">
            <TrophyOutline />
          </n-icon>
          我的比赛
        </h1>
        <p class="page-subtitle">选择比赛开始答题</p>
      </div>
      <n-button @click="loadData" :loading="loading">
        <template #icon><n-icon><RefreshOutline /></n-icon></template>
        刷新
      </n-button>
    </div>
    
    <n-card>
      <n-alert type="success" title="选手提示" style="margin-bottom: 16px;">
        只有进行中的比赛才能参加。点击"进入比赛"开始答题和抢答。
      </n-alert>
      
      <n-data-table
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="false"
        striped
      />
      
      <n-empty v-if="!loading && data.length === 0" description="暂无可参加的比赛" />
    </n-card>
  </div>
</template>

<style lang="scss" scoped>
.player-sessions {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    
    .page-title {
      margin: 0 0 4px 0;
      font-size: 24px;
      font-weight: 600;
      display: flex;
      align-items: center;
    }
    
    .page-subtitle {
      margin: 0;
      color: #999;
      font-size: 14px;
    }
  }
}
</style>
