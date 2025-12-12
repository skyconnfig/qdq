<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, useDialog, NButton, NTag, NSpace, NAvatar, NIcon, type DataTableColumns } from 'naive-ui'
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, resetUserPassword, type User, type UserQuery } from '@/api/modules'
import { AddOutline, RefreshOutline, SearchOutline, TrashOutline, KeyOutline } from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

// 表格数据
const loading = ref(false)
const data = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<UserQuery>({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null
})

// 表单弹窗
const showModal = ref(false)
const modalTitle = ref('新建用户')
const formData = reactive<User>({
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

// 重置密码弹窗
const showResetPwdModal = ref(false)
const resetPwdData = reactive({
  userId: null as number | null,
  username: '',
  newPassword: ''
})

// 批量操作
const selectedRowKeys = ref<number[]>([])

// 表单验证规则
const formRules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  name: {
    required: true,
    message: '请输入姓名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur',
    validator: (_rule: any, value: string) => {
      if (!formData.id && !value) {
        return new Error('请输入密码')
      }
      return true
    }
  }
}

// 表格列定义
const columns: DataTableColumns<any> = [
  {
    type: 'selection'
  } as any,
  {
    title: '用户',
    key: 'user',
    render: (row: any) => h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
      h(NAvatar, { size: 'small', round: true, src: row.avatar }, () => row.name?.charAt(0) || 'U'),
      h('div', {}, [
        h('div', { style: 'font-weight: 500;' }, row.name || '-'),
        h('div', { style: 'font-size: 12px; color: #999;' }, row.username)
      ])
    ])
  },
  {
    title: '手机号',
    key: 'phone',
    render: (row: any) => row.phone || '-'
  },
  {
    title: '邮箱',
    key: 'email',
    render: (row: any) => row.email || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => h(NTag, {
      type: row.status === 1 ? 'success' : 'error',
      size: 'small'
    }, () => row.status === 1 ? '启用' : '禁用')
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right' as const,
    render: (row: any) => h(NSpace, { size: 'small' }, () => [
      h(NButton, {
        size: 'small',
        quaternary: true,
        type: 'primary',
        onClick: () => handleEdit(row)
      }, () => '编辑'),
      h(NButton, {
        size: 'small',
        quaternary: true,
        type: 'warning',
        onClick: () => handleResetPassword(row)
      }, () => h(NSpace, { size: 4, align: 'center' }, () => [
        h(NIcon, { component: KeyOutline }),
        '重置密码'
      ])),
      h(NButton, {
        size: 'small',
        quaternary: true,
        type: 'error',
        onClick: () => handleDelete(row)
      }, () => '删除')
    ])
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    data.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error: any) {
    message.error(error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page: number) => {
  queryParams.page = page
  loadData()
}

// 新建
const handleCreate = () => {
  modalTitle.value = '新建用户'
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    name: '',
    phone: '',
    email: '',
    status: 1,
    roleIds: []
  })
  showModal.value = true
}

// 编辑
const handleEdit = (row: any) => {
  modalTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    name: row.name,
    phone: row.phone,
    email: row.email,
    status: row.status,
    roleIds: row.roleIds || []
  })
  showModal.value = true
}

// 删除
const handleDelete = (row: any) => {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除用户"${row.name}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(row.id)
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
    message.warning('请选择要删除的用户')
    return
  }
  
  dialog.warning({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteUsers(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error: any) {
        message.error(error.message)
      }
    }
  })
}

// 重置密码
const handleResetPassword = (row: any) => {
  resetPwdData.userId = row.id
  resetPwdData.username = row.username
  resetPwdData.newPassword = ''
  showResetPwdModal.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPwdData.newPassword) {
    message.warning('请输入新密码')
    return
  }
  
  try {
    await resetUserPassword(resetPwdData.userId!, resetPwdData.newPassword)
    message.success('密码重置成功')
    showResetPwdModal.value = false
  } catch (error: any) {
    message.error(error.message)
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (formData.id) {
      await updateUser(formData.id, formData)
      message.success('更新成功')
    } else {
      await createUser(formData)
      message.success('创建成功')
    }
    showModal.value = false
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
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <n-space>
        <n-button
          v-if="selectedRowKeys.length > 0"
          type="error"
          @click="handleBatchDelete"
        >
          <template #icon>
            <n-icon><TrashOutline /></n-icon>
          </template>
          批量删除 ({{ selectedRowKeys.length }})
        </n-button>
        <n-button type="primary" @click="handleCreate">
          <template #icon>
            <n-icon><AddOutline /></n-icon>
          </template>
          新建用户
        </n-button>
      </n-space>
    </div>
    
    <!-- 搜索栏 -->
    <n-card class="mb-md">
      <n-space>
        <n-input
          v-model:value="queryParams.keyword"
          placeholder="搜索用户名/姓名/手机号"
          style="width: 240px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon><SearchOutline /></n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="queryParams.status"
          placeholder="状态筛选"
          style="width: 120px"
          clearable
          :options="[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 }
          ]"
        />
        <n-button type="primary" @click="handleSearch">
          搜索
        </n-button>
        <n-button @click="handleReset">
          <template #icon>
            <n-icon><RefreshOutline /></n-icon>
          </template>
          重置
        </n-button>
      </n-space>
    </n-card>
    
    <!-- 数据表格 -->
    <n-card>
      <n-data-table
        v-model:checked-row-keys="selectedRowKeys"
        :columns="columns"
        :data="data"
        :loading="loading"
        :row-key="(row: any) => row.id"
        :pagination="{
          page: queryParams.page,
          pageSize: queryParams.pageSize,
          itemCount: total,
          showSizePicker: true,
          pageSizes: [10, 20, 50],
          onChange: handlePageChange
        }"
        striped
      />
    </n-card>
    
    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="modalTitle"
      style="width: 500px"
    >
      <n-form
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :disabled="!!formData.id"
          />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            :placeholder="formData.id ? '留空则不修改密码' : '请输入密码'"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      
      <template #action>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
    
    <!-- 重置密码弹窗 -->
    <n-modal
      v-model:show="showResetPwdModal"
      preset="card"
      title="重置密码"
      style="width: 450px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="用户名">
          <n-input :value="resetPwdData.username" disabled />
        </n-form-item>
        <n-form-item label="新密码">
          <n-input
            v-model:value="resetPwdData.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password-on="click"
          />
        </n-form-item>
      </n-form>
      
      <template #action>
        <n-space justify="end">
          <n-button @click="showResetPwdModal = false">取消</n-button>
          <n-button type="primary" @click="handleResetPasswordSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style lang="scss" scoped>
.user-management {
  // 自定义样式
}
</style>
