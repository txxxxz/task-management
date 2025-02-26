<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

onMounted(async () => {
  // 只有当token存在时才尝试获取用户信息
  if (userStore.token) {
    console.log('应用启动时检测到token，尝试获取用户信息')
    try {
      await userStore.fetchUserInfo()
      console.log('用户信息获取成功:', userStore.userInfo)
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      
      // 如果获取用户信息失败，清除token和用户信息
      userStore.clearUserInfo()
      
      // 显示错误消息但不中断用户体验
      ElMessage.warning('身份信息已过期，请重新登录')
    }
  } else {
    console.log('未检测到token')
  }
})
</script>

<template>
  <router-view />
</template>

<style>
@import './assets/main.css';

html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
