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
        <el-dropdown>
          <el-avatar :src="userAvatar" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item>设置</el-dropdown-item>
              <el-dropdown-item divided>退出登录</el-dropdown-item>
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
        <el-menu-item index="/workplace">
          <el-icon><Office /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/data">
          <el-icon><DataLine /></el-icon>
          <span>数据可视化</span>
        </el-menu-item>
        <el-menu-item index="/list">
          <el-icon><List /></el-icon>
          <span>列表页</span>
        </el-menu-item>
        <el-menu-item index="/table">
          <el-icon><Grid /></el-icon>
          <span>表格页</span>
        </el-menu-item>
        <el-menu-item index="/detail">
          <el-icon><Document /></el-icon>
          <span>详情页</span>
        </el-menu-item>
        <el-menu-item index="/result">
          <el-icon><CircleCheck /></el-icon>
          <span>结果页</span>
        </el-menu-item>
        <el-menu-item index="/exception">
          <el-icon><Warning /></el-icon>
          <span>异常页</span>
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

const searchKeyword = ref('')
const isDark = ref(false)
const isCollapse = ref(false)
const activeMenu = ref('/dashboard')
const userAvatar = ref('https://placeholder.com/150')
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