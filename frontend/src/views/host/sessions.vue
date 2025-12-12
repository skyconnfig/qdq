<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NTag, type DataTableColumns } from 'naive-ui'
import { getSessionList, type SessionQuery } from '@/api/modules'
import { PlayOutline, DesktopOutline, RefreshOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref<any[]>([])

const statusMap: Record<number, { type: 'default' | 'info' | 'success' | 'warning' | 'error'; text: string }> = {
  0: { type: 'default', text: '草稿' },
  1: { type: 'info', text: '待开始' },
  2: { type: 'success', text: '进行中' },
  3: { type: 'warning', text: '暂停' },
  4: { type: 'error', text: '已结束' }
}

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
      const status = statusMap[row.status] || statusMap[0]
      return h(NTag, { type: status.type, size: 'small' }, () => status.text)
    }
  },
  { title: '题目数', key: 'questionCount', width: 100 },
  { title: '计划开始时间', key: 'scheduledStart', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right' as const,
    render: (row: any) => h('div', { style: 'display: flex; gap: 8px;' }, [
      (row.status === 1 || row.status === 2 || row.status === 3) ? h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => router.push(`/host/control/${row.id}`)
      }, () => h('span', { style: 'display: flex; align-items: center; gap: 4px;' }, [
        h(PlayOutline),
        '进入控制台'
      ])) : null,
      h(NButton, {
        size: 'small',
        type: 'info',
        onClick: () => window.open(`/bigscreen/${row.id}`, '_blank')
      }, () => h('span', { style: 'display: flex; align-items: center; gap: 4px;' }, [
        h(DesktopOutline),
        '大屏'
      ]))
    ].filter(Boolean))
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const params: SessionQuery = {
      page: 1,
      pageSize: 100,
      status: undefined // 显示所有状态的比赛
    }
    const res = await getSessionList(params)
    data.value = res.data.records || []
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
  <div class="host-sessions">
    <div class="page-header">
      <h1 class="page-title">我的比赛</h1>
      <n-button @click="loadData" :loading="loading">
        <template #icon><n-icon><RefreshOutline /></n-icon></template>
        刷新
      </n-button>
    </div>
    
    <n-card>
      <n-alert type="info" title="主持人提示" style="margin-bottom: 16px;">
        您可以控制待开始、进行中或暂停的比赛。点击"进入控制台"开始主持比赛。
      </n-alert>
      
      <n-data-table
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="false"
        striped
      />
    </n-card>
  </div>
</template>

<style lang="scss" scoped>
.host-sessions {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }
}
</style>
