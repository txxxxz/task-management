<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Task Management System" />
        <span>Task Management System</span>
      </div>
      <div class="header-right">
        <el-input
          placeholder="搜索..."
          prefix-icon="Search"
          v-model="searchKeyword"
          class="search-input"
        />
        <el-button icon="Language" circle />
        <el-button icon="Bell" circle />
        <el-switch
          v-model="isDark"
          inline-prompt
          :active-icon="Moon"
          :inactive-icon="Sunny"
        />
        <el-dropdown @command="handleCommand">
          <el-avatar :src="userAvatar" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/data">
          <el-icon><DataLine /></el-icon>
          <span>数据可视化</span>
        </el-menu-item>
        <el-menu-item index="/list">
          <el-icon><List /></el-icon>
          <span>任务列表</span>
        </el-menu-item>
        <el-menu-item index="/table">
          <el-icon><Grid /></el-icon>
          <span>任务处理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
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
import { ref } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const isDark = ref(false)
const isCollapse = ref(false)
const activeMenu = ref('/dashboard')
const userAvatar = ref('https://placeholder.com/150')

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
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
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
</style> 