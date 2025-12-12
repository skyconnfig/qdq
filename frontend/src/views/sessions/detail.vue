<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, NButton, NTag, NSpace, type DataTableColumns } from 'naive-ui'
import { getSessionDetail, updateSession, startSession, type Session } from '@/api/modules'
import { ArrowBackOutline, CreateOutline, PlayOutline, SaveOutline } from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const sessionId = Number(route.params.id)
const loading = ref(false)
const editMode = ref(false)
const sessionData = ref<Session & { createdAt?: string; updatedAt?: string }>({
  name: '',
  description: '',
  mode: 1,
  questionIds: [],
  status: 0
})

const statusMap: Record<number, { type: any; text: string }> = {
  0: { type: 'default', text: '草稿' },
  1: { type: 'info', text: '待开始' },
  2: { type: 'success', text: '进行中' },
  3: { type: 'warning', text: '暂停' },
  4: { type: 'error', text: '已结束' }
}

const modeOptions = [
  { label: '个人赛', value: 1 },
  { label: '团队赛', value: 2 }
]

// 题目列表列
const questionColumns: DataTableColumns<any> = [
  { title: '序号', key: 'index', width: 80, render: (_row: any, index: number) => index + 1 },
  { title: '题目', key: 'title', ellipsis: { tooltip: true } },
  {
    title: '题型',
    key: 'type',
    width: 100,
    render: (row: any) => {
      const types: Record<number, string> = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' }
      return types[row.type] || '-'
    }
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 100,
    render: (row: any) => {
      const colors: Record<number, 'success' | 'warning' | 'error'> = { 1: 'success', 2: 'warning', 3: 'error' }
      const texts: Record<number, string> = { 1: '简单', 2: '中等', 3: '困难' }
      return h(NTag, { type: colors[row.difficulty], size: 'small' }, () => texts[row.difficulty] || '-')
    }
  },
  { title: '分值', key: 'score', width: 80 }
]

const questionList = ref<any[]>([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getSessionDetail(sessionId)
    sessionData.value = res.data
    // TODO: 加载题目列表
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 切换编辑模式
const toggleEditMode = () => {
  editMode.value = !editMode.value
}

// 保存更新
const handleSave = async () => {
  try {
    await updateSession(sessionId, sessionData.value)
    message.success('保存成功')
    editMode.value = false
    loadData()
  } catch (error: any) {
    message.error(error.message)
  }
}

// 开始比赛
const handleStart = async () => {
  try {
    await startSession(sessionId)
    message.success('比赛已开始')
    loadData()
  } catch (error: any) {
    message.error(error.message)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="session-detail">
    <div class="page-header">
      <n-space align="center">
        <n-button text @click="router.back()">
          <template #icon><n-icon><ArrowBackOutline /></n-icon></template>
          返回
        </n-button>
        <h1 class="page-title">比赛详情</h1>
      </n-space>
      <n-space>
        <n-button v-if="sessionData.status === 0 || sessionData.status === 1" type="success" @click="handleStart">
          <template #icon><n-icon><PlayOutline /></n-icon></template>
          开始比赛
        </n-button>
        <n-button v-if="!editMode" @click="toggleEditMode">
          <template #icon><n-icon><CreateOutline /></n-icon></template>
          编辑
        </n-button>
        <n-button v-if="editMode" type="primary" @click="handleSave">
          <template #icon><n-icon><SaveOutline /></n-icon></template>
          保存
        </n-button>
        <n-button v-if="editMode" @click="toggleEditMode">
          取消
        </n-button>
      </n-space>
    </div>
    
    <n-spin :show="loading">
      <n-space vertical :size="16">
        <!-- 基本信息 -->
        <n-card title="基本信息">
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="比赛名称">
              <n-input v-if="editMode" v-model:value="sessionData.name" placeholder="请输入比赛名称" />
              <span v-else>{{ sessionData.name }}</span>
            </n-descriptions-item>
            <n-descriptions-item label="比赛模式">
              <n-select v-if="editMode" v-model:value="sessionData.mode" :options="modeOptions" />
              <span v-else>{{ modeOptions.find(o => o.value === sessionData.mode)?.label }}</span>
            </n-descriptions-item>
            <n-descriptions-item label="比赛状态">
              <n-tag :type="statusMap[sessionData.status || 0]?.type" size="small">
                {{ statusMap[sessionData.status || 0]?.text }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="题目数量">
              {{ sessionData.questionIds?.length || 0 }} 道
            </n-descriptions-item>
            <n-descriptions-item label="计划开始时间" :span="2">
              {{ sessionData.scheduledStart || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="比赛描述" :span="2">
              <n-input v-if="editMode" v-model:value="sessionData.description" type="textarea" :rows="3" placeholder="请输入比赛描述" />
              <span v-else>{{ sessionData.description || '-' }}</span>
            </n-descriptions-item>
            <n-descriptions-item label="创建时间">
              {{ sessionData.createdAt || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="更新时间">
              {{ sessionData.updatedAt || '-' }}
            </n-descriptions-item>
          </n-descriptions>
        </n-card>
        
        <!-- 题目列表 -->
        <n-card title="题目列表">
          <n-data-table
            :columns="questionColumns"
            :data="questionList"
            :pagination="false"
            size="small"
          />
          <n-empty v-if="questionList.length === 0" description="暂无题目" />
        </n-card>
      </n-space>
    </n-spin>
  </div>
</template>

<style lang="scss" scoped>
.session-detail {
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
}
</style>
