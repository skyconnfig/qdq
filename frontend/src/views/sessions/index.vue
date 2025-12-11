<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog, NButton, NTag, NSpace } from 'naive-ui'
import { http } from '@/api/request'
import { AddOutline, RefreshOutline, SearchOutline, PlayOutline, DesktopOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const data = ref<any[]>([])
const total = ref(0)
const showModal = ref(false)
const modalTitle = ref('新建比赛')

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null as number | null
})

const formData = reactive({
  id: null as number | null,
  name: '',
  description: '',
  mode: 1,
  questionIds: [] as number[],
  scheduledStart: null as string | null
})

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '待开始', value: 1 },
  { label: '进行中', value: 2 },
  { label: '暂停', value: 3 },
  { label: '已结束', value: 4 }
]

const modeOptions = [
  { label: '个人赛', value: 1 },
  { label: '团队赛', value: 2 }
]

const getStatusTag = (status: number) => {
  const map: Record<number, { type: any; text: string }> = {
    0: { type: 'default', text: '草稿' },
    1: { type: 'info', text: '待开始' },
    2: { type: 'success', text: '进行中' },
    3: { type: 'warning', text: '暂停' },
    4: { type: 'error', text: '已结束' }
  }
  return map[status] || { type: 'default', text: '未知' }
}

const columns = [
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
      const tag = getStatusTag(row.status)
      return h(NTag, { type: tag.type, size: 'small' }, () => tag.text)
    }
  },
  { title: '题目数', key: 'questionCount', width: 80 },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    render: (row: any) => h(NSpace, {}, () => [
      h(NButton, {
        size: 'small', quaternary: true, type: 'primary',
        onClick: () => router.push(`/sessions/${row.id}`)
      }, () => '详情'),
      row.status === 2 || row.status === 1 ? h(NButton, {
        size: 'small', quaternary: true, type: 'success',
        onClick: () => router.push(`/sessions/${row.id}/control`)
      }, { default: () => '控制台', icon: () => h(PlayOutline) }) : null,
      h(NButton, {
        size: 'small', quaternary: true, type: 'info',
        onClick: () => window.open(`/bigscreen/${row.id}`, '_blank')
      }, { default: () => '大屏', icon: () => h(DesktopOutline) }),
      row.status === 0 ? h(NButton, {
        size: 'small', quaternary: true, type: 'error',
        onClick: () => handleDelete(row)
      }, () => '删除') : null
    ].filter(Boolean))
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await http.get('/sessions', queryParams)
    data.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.page = 1
  loadData()
}

const handleCreate = () => {
  modalTitle.value = '新建比赛'
  Object.assign(formData, {
    id: null, name: '', description: '', mode: 1, questionIds: [], scheduledStart: null
  })
  showModal.value = true
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除比赛"${row.name}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await http.delete(`/sessions/${row.id}`)
        message.success('删除成功')
        loadData()
      } catch (error: any) {
        message.error(error.message)
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await http.put(`/sessions/${formData.id}`, formData)
      message.success('更新成功')
    } else {
      await http.post('/sessions', formData)
      message.success('创建成功')
    }
    showModal.value = false
    loadData()
  } catch (error: any) {
    message.error(error.message)
  }
}

onMounted(() => loadData())
</script>

<template>
  <div class="session-management">
    <div class="page-header">
      <h1 class="page-title">比赛管理</h1>
      <n-button type="primary" @click="handleCreate">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新建比赛
      </n-button>
    </div>
    
    <n-card class="mb-md">
      <n-space>
        <n-input v-model:value="queryParams.keyword" placeholder="搜索比赛名称" style="width: 200px" clearable @keyup.enter="handleSearch">
          <template #prefix><n-icon><SearchOutline /></n-icon></template>
        </n-input>
        <n-select v-model:value="queryParams.status" placeholder="状态" style="width: 120px" clearable :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset"><template #icon><n-icon><RefreshOutline /></n-icon></template>重置</n-button>
      </n-space>
    </n-card>
    
    <n-card>
      <n-data-table :columns="columns" :data="data" :loading="loading"
        :pagination="{ page: queryParams.page, pageSize: queryParams.pageSize, itemCount: total, onChange: (p: number) => { queryParams.page = p; loadData() } }"
        striped />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 600px">
      <n-form :model="formData" label-placement="left" label-width="80">
        <n-form-item label="比赛名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入比赛名称" />
        </n-form-item>
        <n-form-item label="比赛描述" path="description">
          <n-input v-model:value="formData.description" type="textarea" placeholder="请输入比赛描述" :rows="3" />
        </n-form-item>
        <n-form-item label="比赛模式" path="mode">
          <n-radio-group v-model:value="formData.mode">
            <n-radio v-for="opt in modeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="开始时间" path="scheduledStart">
          <n-date-picker v-model:value="formData.scheduledStart" type="datetime" placeholder="选择计划开始时间" clearable style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
