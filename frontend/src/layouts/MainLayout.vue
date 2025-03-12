<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">
        <img src="/Users/txxxxz/Desktop/斗转星移/final project/task-management/frontend/img/guineahead.svg" alt="Task Management System" />
        <span>Guinea Pig Task Management System</span>
      </div>
      <div class="header-right">
        <el-input
          placeholder="Search..."
          prefix-icon="Search"
          v-model="searchKeyword"
          class="search-input"
        />
        <el-button icon="Bell" circle />
        <el-dropdown @command="handleCommand">
          <el-avatar 
            :size="40"
            :src="userStore.userInfo?.avatar || defaultAvatar"
            class="user-avatar"
          >
            {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
          </el-avatar>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">Personal Center</el-dropdown-item>
              <el-dropdown-item command="settings">Settings</el-dropdown-item>
              <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapse"
        router
      >
        <el-menu-item 
          v-for="item in authorizedMenuItems" 
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Moon, Sunny, Grid, Calendar, DataLine, List, User, Plus, Operation } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { menuItems } from '@/config/menu'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const isDark = ref(false)
const isCollapse = ref(false)
const activeMenu = ref(router.currentRoute.value.path)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 监听路由变化
watch(() => router.currentRoute.value.path, (newPath) => {
  activeMenu.value = newPath
})

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          'Are you sure you want to logout?',
          'Tips',
          {
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )
        await userStore.logout()
      } catch (error) {
        // 用户取消退出登录
      }
      break
  }
}

// 添加菜单权限控制的计算属性
const authorizedMenuItems = computed(() => {
  const userRole = userStore.userInfo?.role?.toString()
  return menuItems.filter(item => {
    // 如果没有指定角色限制,则所有人可见
    if (!item.roles) return true
    // 如果指定了角色限制,则检查用户角色是否在允许的角色列表中
    return item.roles.includes(userRole || '')
  })
})
</script>

<style scoped>
.layout-container {
  display: grid;
  grid-template-areas:
    "header header"
    "sidebar main";
  grid-template-columns: auto 1fr;
  grid-template-rows: 60px 1fr;
  min-height: 100vh;
}

.header {
  grid-area: header;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo img {
  height: 32px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.sidebar {
  grid-area: sidebar;
  border-right: 1px solid var(--el-border-color-light);
  height: calc(100vh - 60px);
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.main-content {
  grid-area: main;
  padding: 20px;
  background: var(--el-bg-color-page);
}

.user-avatar {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.user-avatar:hover {
  transform: scale(1.1);
  border-color: var(--el-color-primary);
}
</style> 