<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const loginType = ref<'account' | 'phone'>('account')

const formData = reactive({
  username: '',
  password: '',
  phone: '',
  code: '',
  rememberMe: false
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  },
  phone: {
    required: true,
    pattern: /^1[3-9]\d{9}$/,
    message: '请输入正确的手机号',
    trigger: 'blur'
  },
  code: {
    required: true,
    message: '请输入验证码',
    trigger: 'blur'
  }
}

const handleLogin = async () => {
  if (loginType.value === 'account') {
    if (!formData.username || !formData.password) {
      message.warning('请填写用户名和密码')
      return
    }
  }
  
  loading.value = true
  try {
    await userStore.login({
      username: formData.username,
      password: formData.password,
      rememberMe: formData.rememberMe
    })
    
    message.success('登录成功')
    
    // 跳转到之前的页面或首页
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 轮播图数据
const bannerList = [
  {
    title: '知识竞赛抢答系统',
    subtitle: '低延迟 · 高可靠 · 多人并发',
    image: '/images/banner1.svg'
  },
  {
    title: '实时抢答',
    subtitle: '毫秒级响应，公平公正',
    image: '/images/banner2.svg'
  },
  {
    title: '多端支持',
    subtitle: 'Web / H5 / Android 全覆盖',
    image: '/images/banner3.svg'
  }
]
</script>

<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-banner">
      <n-carousel autoplay show-arrow>
        <div v-for="(item, index) in bannerList" :key="index" class="banner-item">
          <div class="banner-content">
            <div class="banner-icon">
              <n-icon size="80" color="#18A058">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </n-icon>
            </div>
            <h1 class="banner-title">{{ item.title }}</h1>
            <p class="banner-subtitle">{{ item.subtitle }}</p>
          </div>
        </div>
      </n-carousel>
      
      <div class="banner-footer">
        <p>© 2024 知识竞赛抢答系统</p>
      </div>
    </div>
    
    <!-- 右侧登录区域 -->
    <div class="login-form-wrapper">
      <div class="login-form-container">
        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-subtitle">知识竞赛抢答系统管理平台</p>
        </div>
        
        <!-- 登录方式切换 -->
        <n-tabs v-model:value="loginType" type="line" animated>
          <n-tab-pane name="account" tab="账号登录">
            <n-form :rules="rules" class="login-form">
              <n-form-item path="username">
                <n-input
                  v-model:value="formData.username"
                  placeholder="请输入用户名"
                  size="large"
                  :input-props="{ autocomplete: 'username' }"
                >
                  <template #prefix>
                    <n-icon :size="18" color="#999">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                      </svg>
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>
              
              <n-form-item path="password">
                <n-input
                  v-model:value="formData.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password-on="click"
                  :input-props="{ autocomplete: 'current-password' }"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <n-icon :size="18" color="#999">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                      </svg>
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>
              
              <div class="login-options">
                <n-checkbox v-model:checked="formData.rememberMe">
                  记住我
                </n-checkbox>
                <n-button text type="primary" size="small">
                  忘记密码？
                </n-button>
              </div>
              
              <n-button
                type="primary"
                size="large"
                block
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </n-button>
            </n-form>
          </n-tab-pane>
          
          <n-tab-pane name="phone" tab="短信登录">
            <n-form :rules="rules" class="login-form">
              <n-form-item path="phone">
                <n-input
                  v-model:value="formData.phone"
                  placeholder="请输入手机号"
                  size="large"
                >
                  <template #prefix>
                    <n-icon :size="18" color="#999">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/>
                      </svg>
                    </n-icon>
                  </template>
                </n-input>
              </n-form-item>
              
              <n-form-item path="code">
                <n-input-group>
                  <n-input
                    v-model:value="formData.code"
                    placeholder="请输入验证码"
                    size="large"
                    style="flex: 1"
                  />
                  <n-button size="large" style="width: 120px">
                    获取验证码
                  </n-button>
                </n-input-group>
              </n-form-item>
              
              <n-button
                type="primary"
                size="large"
                block
                :loading="loading"
              >
                登 录
              </n-button>
            </n-form>
          </n-tab-pane>
        </n-tabs>
        
        <div class="login-footer">
          <p class="footer-text">
            登录即表示同意
            <n-button text type="primary" size="small">用户协议</n-button>
            和
            <n-button text type="primary" size="small">隐私政策</n-button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: $bg-body;
}

// 左侧品牌区域
.login-banner {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, $primary 0%, darken($primary, 15%) 100%);
  padding: 40px;
  position: relative;
  
  @media (max-width: $breakpoint-md) {
    display: none;
  }
  
  .banner-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 400px;
  }
  
  .banner-content {
    text-align: center;
    color: white;
    
    .banner-icon {
      margin-bottom: 24px;
      
      :deep(svg) {
        filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
      }
    }
    
    .banner-title {
      font-size: 32px;
      font-weight: 600;
      margin-bottom: 12px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
    
    .banner-subtitle {
      font-size: 16px;
      opacity: 0.9;
    }
  }
  
  .banner-footer {
    position: absolute;
    bottom: 24px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
  }
}

// 右侧登录区域
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: $bg-card;
  
  @media (max-width: $breakpoint-md) {
    padding: 20px;
  }
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .login-title {
    font-size: 28px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  .login-subtitle {
    font-size: 14px;
    color: $text-secondary;
  }
}

.login-form {
  margin-top: 24px;
  
  :deep(.n-form-item) {
    margin-bottom: 20px;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  
  .footer-text {
    font-size: 12px;
    color: $text-secondary;
  }
}

// 响应式：小屏幕时的样式
@media (max-width: $breakpoint-md) {
  .login-container {
    flex-direction: column;
  }
  
  .login-form-wrapper {
    min-height: 100vh;
  }
}
</style>
