<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import {
  HomeOutline,
  PeopleOutline,
  LibraryOutline,
  TrophyOutline,
  SettingsOutline,
  MenuOutline,
  LogOutOutline,
  PersonOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

const collapsed = ref(false)

// 菜单图标映射
const iconMap: Record<string, any> = {
  HomeOutline,
  PeopleOutline,
  LibraryOutline,
  TrophyOutline,
  SettingsOutline
}

// 渲染图标
const renderIcon = (iconName: string) => {
  const icon = iconMap[iconName]
  return icon ? () => h(NIcon, null, { default: () => h(icon) }) : undefined
}

// 菜单配置
const menuOptions = computed(() => [
  {
    label: '工作台',
    key: '/dashboard',
    icon: renderIcon('HomeOutline')
  },
  {
    label: '用户管理',
    key: '/users',
    icon: renderIcon('PeopleOutline')
  },
  {
    label: '题库管理',
    key: '/questions',
    icon: renderIcon('LibraryOutline')
  },
  {
    label: '比赛管理',
    key: '/sessions',
    icon: renderIcon('TrophyOutline')
  },
  {
    label: '系统设置',
    key: '/settings',
    icon: renderIcon('SettingsOutline')
  }
])

// 当前选中菜单
const activeKey = computed(() => {
  const path = route.path
  // 匹配一级路由
  const matched = menuOptions.value.find(item => path.startsWith(item.key))
  return matched?.key || path
})

// 菜单点击
const handleMenuClick = (key: string) => {
  router.push(key)
}

// 用户下拉菜单
const userDropdownOptions = [
  {
    label: '个人信息',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
]

const handleUserAction = async (key: string) => {
  if (key === 'logout') {
    await userStore.logout()
    message.success('已退出登录')
    router.push('/login')
  } else if (key === 'profile') {
    // TODO: 跳转个人信息页面
  }
}
</script>

<template>
  <n-layout has-sider class="admin-layout">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="admin-sider"
    >
      <!-- Logo -->
      <div class="sider-logo">
        <div class="logo-icon">
          <n-icon size="28" color="#18A058">
            <TrophyOutline />
          </n-icon>
        </div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">知识竞赛系统</span>
        </transition>
      </div>
      
      <!-- 菜单 -->
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>
    
    <!-- 主内容区域 -->
    <n-layout>
      <!-- 顶部导航 -->
      <n-layout-header bordered class="admin-header">
        <div class="header-left">
          <n-button quaternary circle @click="collapsed = !collapsed">
            <template #icon>
              <n-icon size="20">
                <MenuOutline />
              </n-icon>
            </template>
          </n-button>
          
          <!-- 面包屑 -->
          <n-breadcrumb class="breadcrumb">
            <n-breadcrumb-item>
              <router-link to="/">首页</router-link>
            </n-breadcrumb-item>
            <n-breadcrumb-item v-if="route.meta.title">
              {{ route.meta.title }}
            </n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 用户信息 -->
          <n-dropdown
            :options="userDropdownOptions"
            @select="handleUserAction"
          >
            <div class="user-info">
              <n-avatar
                round
                size="small"
                :src="userStore.userInfo?.avatar"
              >
                {{ userStore.userInfo?.name?.charAt(0) || 'U' }}
              </n-avatar>
              <span class="user-name">{{ userStore.userInfo?.name || '用户' }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>
      
      <!-- 内容区域 -->
      <n-layout-content class="admin-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
}

.admin-sider {
  :deep(.n-layout-sider-scroll-container) {
    display: flex;
    flex-direction: column;
  }
}

.sider-logo {
  height: $header-height;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid $border-color;
  
  .logo-icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .logo-text {
    margin-left: 12px;
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
  }
}

.admin-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: $bg-card;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .breadcrumb {
    margin-left: 8px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $border-radius;
    transition: background $transition-fast;
    
    &:hover {
      background: $bg-hover;
    }
    
    .user-name {
      font-size: 14px;
      color: $text-primary;
    }
  }
}

.admin-content {
  background: $bg-body;
  
  .content-wrapper {
    padding: $spacing-lg;
    min-height: calc(100vh - #{$header-height});
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
