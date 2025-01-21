import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types/models'
import * as userApi from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  const login = async (username: string, password: string) => {
    try {
      const tokenValue = await userApi.login({ username, password })
      token.value = tokenValue
      localStorage.setItem('token', tokenValue)
      await getUserInfo()
      return true
    } catch (error) {
      return false
    }
  }

  const getUserInfo = async () => {
    try {
      const data = await userApi.getUserInfo()
      user.value = data
      return true
    } catch (error) {
      return false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  const updateInfo = async (data: Partial<User>) => {
    try {
      await userApi.updateUserInfo(data)
      await getUserInfo()
      ElMessage.success('更新成功')
      return true
    } catch (error) {
      return false
    }
  }

  return {
    user,
    token,
    login,
    logout,
    getUserInfo,
    updateInfo
  }
}) 