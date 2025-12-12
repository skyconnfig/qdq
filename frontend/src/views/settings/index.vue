<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/modules'
import { useRouter } from 'vue-router'
import RoleSwitcher from '@/components/RoleSwitcher.vue'

const message = useMessage()
const userStore = useUserStore()
const router = useRouter()

// 修改密码表单
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: {
    required: true,
    message: '请输入当前密码',
    trigger: 'blur'
  },
  newPassword: {
    required: true,
    message: '请输入新密码',
    trigger: 'blur',
    validator: (_rule: any, value: string) => {
      if (!value) {
        return new Error('请输入新密码')
      }
      if (value.length < 6) {
        return new Error('密码长度不能少于6位')
      }
      return true
    }
  },
  confirmPassword: {
    required: true,
    message: '请确认新密码',
    trigger: 'blur',
    validator: (_rule: any, value: string) => {
      if (!value) {
        return new Error('请确认新密码')
      }
      if (value !== passwordForm.newPassword) {
        return new Error('两次密码输入不一致')
      }
      return true
    }
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    message.success('密码修改成功，请重新登录')
    
    // 清空表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 登出并跳转到登录页
    setTimeout(async () => {
      await userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (error: any) {
    if (error?.message) {
      message.error(error.message)
    }
  }
}
</script>

<template>
  <div class="settings">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
    </div>
    
    <n-space vertical :size="16">
      <!-- 角色切换（开发测试） -->
      <RoleSwitcher />
      
      <!-- 修改密码 -->
      <n-card title="修改密码">
        <n-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-placement="left"
          label-width="120"
          style="max-width: 600px"
        >
          <n-form-item label="当前密码" path="oldPassword">
            <n-input
              v-model:value="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password-on="click"
            />
          </n-form-item>
          <n-form-item label="新密码" path="newPassword">
            <n-input
              v-model:value="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（不少于6位）"
              show-password-on="click"
            />
          </n-form-item>
          <n-form-item label="确认密码" path="confirmPassword">
            <n-input
              v-model:value="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password-on="click"
              @keyup.enter="handleChangePassword"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleChangePassword">
              修改密码
            </n-button>
          </n-form-item>
        </n-form>
      </n-card>
      
      <!-- 基本设置 -->
      <n-card title="基本设置">
        <n-form label-placement="left" label-width="120" style="max-width: 600px">
        <n-form-item label="抢答超时时间">
          <n-input-number :default-value="100" :min="50" :max="500" style="width: 200px">
            <template #suffix>毫秒</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="默认答题时限">
          <n-input-number :default-value="30" :min="10" :max="120" style="width: 200px">
            <template #suffix>秒</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="答对得分">
          <n-input-number :default-value="10" :min="1" style="width: 200px">
            <template #suffix>分</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="答错得分">
          <n-input-number :default-value="0" style="width: 200px">
            <template #suffix>分</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="启用抢答音效">
          <n-switch :default-value="true" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary">保存设置</n-button>
        </n-form-item>
        </n-form>
      </n-card>
    </n-space>
  </div>
</template>
