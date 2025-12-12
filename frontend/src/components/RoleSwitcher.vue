<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const message = useMessage()
const router = useRouter()

const roles = [
  { label: '超级管理员', value: 'SUPER_ADMIN', description: '拥有所有权限' },
  { label: '主持人', value: 'HOST', description: '控制比赛流程' },
  { label: '评委', value: 'JUDGE', description: '评判主观题' },
  { label: '选手', value: 'PLAYER', description: '参赛选手' },
  { label: '观众', value: 'VIEWER', description: '观看比赛' }
]

const currentRole = ref(userStore.userInfo?.roles?.[0] || 'SUPER_ADMIN')

const handleSwitch = (roleCode: string) => {
  // 模拟切换角色（实际应该调用后端API）
  if (userStore.userInfo) {
    userStore.userInfo.roles = [roleCode]
  }
  currentRole.value = roleCode
  message.success(`已切换到${roles.find(r => r.value === roleCode)?.label}`)
  
  // 刷新页面以应用新角色
  setTimeout(() => {
    router.push('/').then(() => {
      window.location.reload()
    })
  }, 500)
}
</script>

<template>
  <n-card title="角色切换 (开发测试)" size="small">
    <n-alert type="warning" title="提示" style="margin-bottom: 16px;">
      这是开发测试功能，用于演示不同角色的页面效果。生产环境中角色由后端API返回。
    </n-alert>
    
    <n-space vertical>
      <div v-for="role in roles" :key="role.value">
        <n-button
          :type="currentRole === role.value ? 'primary' : 'default'"
          block
          @click="handleSwitch(role.value)"
          style="justify-content: flex-start; text-align: left;"
        >
          <div>
            <div style="font-weight: 600;">{{ role.label }}</div>
            <div style="font-size: 12px; opacity: 0.7;">{{ role.description }}</div>
          </div>
        </n-button>
      </div>
    </n-space>
  </n-card>
</template>
