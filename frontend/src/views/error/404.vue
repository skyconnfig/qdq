<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <div class="error-actions">
        <n-button type="primary" @click="goHome">返回首页</n-button>
        <n-button @click="goBack">返回上页</n-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-body;
}

.error-content {
  text-align: center;
  
  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: $primary;
    line-height: 1;
    margin-bottom: 16px;
  }
  
  .error-title {
    font-size: 24px;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  .error-desc {
    color: $text-secondary;
    margin-bottom: 32px;
  }
  
  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
  }
}
</style>
