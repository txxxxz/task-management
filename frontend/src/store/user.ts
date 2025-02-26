import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginVO, UserVO } from '@/types/user'
import * as userApi from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserVO | null>(null)
  const token = ref<string>('')

  const login = async (username: string, password: string, role: number) => {
    try {
      const response = await userApi.login({ username, password, role})
      token.value = response.token
      localStorage.setItem('token', response.token)
      await getUserInfo()
      return true
    } catch (error) {
      return false
    }
  }

  const getUserInfo = async () => {
    try {
      const response = await userApi.getUserInfo()
      user.value = {
        ...response,
        id: Number(response.id),
        status: Number(response.status),
        role: Number(response.role)
      }
      return true
    } catch (error) {
      return false
    }
  }

  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  const updateInfo = async (loginData: LoginVO) => {
    user.value = loginData.user
    token.value = loginData.token
  }

  const clearInfo = () => {
    user.value = null
    token.value = ''
  }

  return {
    user,
    token,
    login,
    logout,
    getUserInfo,
    updateInfo,
    clearInfo
  }
}) 