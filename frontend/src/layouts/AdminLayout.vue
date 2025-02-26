<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <img :src="logo" alt="Logo" />
        <span>Task Management System</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        :router="true"
      >
        <el-menu-item v-if="userStore.userInfo?.role === 2" index="/admin/employee">
          <el-icon><UserFilled /></el-icon>
          <span>Employee Management</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>Back to Home</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-profile">
              {{ userStore.userInfo?.username }}
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">Personal Center</el-dropdown-item>
                <el-dropdown-item command="logout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { House, UserFilled, CaretBottom } from '@element-plus/icons-vue'
import logo from '@/assets/logo.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 18px;
  font-weight: bold;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical .el-menu-item {
  display: flex;
  align-items: center;
}

.el-menu-vertical .el-icon {
  margin-right: 12px;
}
</style> 