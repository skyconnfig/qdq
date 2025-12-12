<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, useDialog, NButton, NTag, NSpace, type DataTableColumns } from 'naive-ui'
import { getQuestionList, createQuestion, updateQuestion, deleteQuestion, batchDeleteQuestions, updateQuestionStatus, type Question, type QuestionQuery } from '@/api/modules'
import { AddOutline, RefreshOutline, SearchOutline, TrashOutline, CheckmarkCircleOutline, ShuffleOutline } from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const data = ref<any[]>([])
const total = ref(0)
const showModal = ref(false)
const modalTitle = ref('新建题目')

const queryParams = reactive<QuestionQuery>({
  page: 1,
  pageSize: 10,
  keyword: '',
  type: null,
  difficulty: null,
  status: null
})

const formData = reactive<Question>({
  type: 1,
  title: '',
  content: '',
  options: [
    { key: 'A', value: '' },
    { key: 'B', value: '' },
    { key: 'C', value: '' },
    { key: 'D', value: '' }
  ],
  answer: '',
  analysis: '',
  score: 10,
  difficulty: 2,
  tags: [],
  status: 0
})

// 批量操作
const selectedRowKeys = ref<number[]>([])

const typeOptions = [
  { label: '单选题', value: 1 },
  { label: '多选题', value: 2 },
  { label: '判断题', value: 3 },
  { label: '填空题', value: 4 },
  { label: '主观题', value: 5 }
]

const difficultyOptions = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 }
]

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '待审核', value: 2 }
]

const columns: DataTableColumns<any> = [
  {
    type: 'selection'
  } as any,
  { title: '题目', key: 'title', ellipsis: { tooltip: true } },
  {
    title: '题型',
    key: 'type',
    width: 100,
    render: (row: any) => {
      const opt = typeOptions.find(o => o.value === row.type)
      return opt?.label || '-'
    }
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 80,
    render: (row: any) => {
      const colors: Record<number, 'success' | 'warning' | 'error'> = { 1: 'success', 2: 'warning', 3: 'error' }
      const opt = difficultyOptions.find(o => o.value === row.difficulty)
      return h(NTag, { type: colors[row.difficulty], size: 'small' }, () => opt?.label || '-')
    }
  },
  { title: '分值', key: 'score', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) => {
      const types: Record<number, 'default' | 'success' | 'warning'> = { 0: 'default', 1: 'success', 2: 'warning' }
      const opt = statusOptions.find(o => o.value === row.status)
      return h(NTag, { type: types[row.status], size: 'small' }, () => opt?.label || '-')
    }
  },
  { title: '创建时间', key: 'createdAt', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row: any) => h(NSpace, {}, () => [
      h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, () => '删除')
    ])
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getQuestionList(queryParams)
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
  queryParams.type = null
  queryParams.difficulty = null
  queryParams.status = null
  queryParams.page = 1
  loadData()
}

const handleCreate = () => {
  modalTitle.value = '新建题目'
  Object.assign(formData, {
    id: undefined, type: 1, title: '', content: '',
    options: [{ key: 'A', value: '' }, { key: 'B', value: '' }, { key: 'C', value: '' }, { key: 'D', value: '' }],
    answer: '', analysis: '', score: 10, difficulty: 2, tags: [], status: 0
  })
  showModal.value = true
}

const handleEdit = (row: any) => {
  modalTitle.value = '编辑题目'
  Object.assign(formData, row)
  showModal.value = true
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '删除确认',
    content: '确定要删除这道题目吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteQuestion(row.id)
        message.success('删除成功')
        loadData()
      } catch (error: any) {
        message.error(error.message)
      }
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的题目')
    return
  }
  
  dialog.warning({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 道题目吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteQuestions(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error: any) {
        message.error(error.message)
      }
    }
  })
}

// 批量更新状态
const handleBatchUpdateStatus = (status: number) => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要更新的题目')
    return
  }
  
  const statusText = statusOptions.find(o => o.value === status)?.label
  
  dialog.info({
    title: '批量更新状态',
    content: `确定要将选中的 ${selectedRowKeys.value.length} 道题目的状态更新为"${statusText}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 批量更新，逐个调用
        await Promise.all(selectedRowKeys.value.map(id => updateQuestionStatus(id, status)))
        message.success('批量更新成功')
        selectedRowKeys.value = []
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
      await updateQuestion(formData.id, formData)
      message.success('更新成功')
    } else {
      await createQuestion(formData)
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
  <div class="question-management">
    <div class="page-header">
      <h1 class="page-title">题库管理</h1>
      <n-space>
        <n-dropdown
          v-if="selectedRowKeys.length > 0"
          trigger="hover"
          :options="[
            { label: '批量删除', key: 'delete', icon: () => h(TrashOutline) },
            { label: '设为草稿', key: 'status-0', icon: () => h(CheckmarkCircleOutline) },
            { label: '设为已发布', key: 'status-1', icon: () => h(CheckmarkCircleOutline) }
          ]"
          @select="(key: string) => {
            if (key === 'delete') handleBatchDelete()
            else if (key.startsWith('status-')) handleBatchUpdateStatus(Number(key.split('-')[1]))
          }"
        >
          <n-button>
            批量操作 ({{ selectedRowKeys.length }})
          </n-button>
        </n-dropdown>
        <n-button type="primary" @click="handleCreate">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          新建题目
        </n-button>
      </n-space>
    </div>
    
    <n-card class="mb-md">
      <n-space>
        <n-input v-model:value="queryParams.keyword" placeholder="搜索题目" style="width: 200px" clearable @keyup.enter="handleSearch">
          <template #prefix><n-icon><SearchOutline /></n-icon></template>
        </n-input>
        <n-select v-model:value="queryParams.type" placeholder="题型" style="width: 120px" clearable :options="typeOptions" />
        <n-select v-model:value="queryParams.difficulty" placeholder="难度" style="width: 100px" clearable :options="difficultyOptions" />
        <n-select v-model:value="queryParams.status" placeholder="状态" style="width: 100px" clearable :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset"><template #icon><n-icon><RefreshOutline /></n-icon></template>重置</n-button>
      </n-space>
    </n-card>
    
    <n-card>
      <n-data-table
        v-model:checked-row-keys="selectedRowKeys"
        :columns="columns"
        :data="data"
        :loading="loading"
        :row-key="(row: any) => row.id"
        :pagination="{ page: queryParams.page, pageSize: queryParams.pageSize, itemCount: total, onChange: (p: number) => { queryParams.page = p; loadData() } }"
        striped />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 700px">
      <n-form :model="formData" label-placement="left" label-width="80">
        <n-form-item label="题型" path="type">
          <n-select v-model:value="formData.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="题目" path="title">
          <n-input v-model:value="formData.title" type="textarea" placeholder="请输入题目" :rows="3" />
        </n-form-item>
        <n-form-item v-if="[1,2].includes(formData.type)" label="选项">
          <div class="options-list">
            <div v-for="(opt, index) in formData.options" :key="index" class="option-item">
              <span class="option-key">{{ opt.key }}</span>
              <n-input v-model:value="opt.value" placeholder="请输入选项内容" />
            </div>
          </div>
        </n-form-item>
        <n-form-item label="答案" path="answer">
          <n-input v-model:value="formData.answer" placeholder="请输入正确答案" />
        </n-form-item>
        <n-form-item label="解析" path="analysis">
          <n-input v-model:value="formData.analysis" type="textarea" placeholder="请输入答案解析" :rows="2" />
        </n-form-item>
        <n-grid :cols="3" :x-gap="16">
          <n-grid-item>
            <n-form-item label="分值"><n-input-number v-model:value="formData.score" :min="1" /></n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="难度"><n-select v-model:value="formData.difficulty" :options="difficultyOptions" /></n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="状态"><n-select v-model:value="formData.status" :options="statusOptions" /></n-form-item>
          </n-grid-item>
        </n-grid>
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

<style lang="scss" scoped>
.options-list {
  width: 100%;
  .option-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
    .option-key {
      font-weight: 600;
      color: $primary;
      width: 24px;
    }
  }
}
</style>
