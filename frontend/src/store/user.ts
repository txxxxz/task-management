import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginVO, UserVO } from '@/types/user'
import * as userApi from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserVO | null>(null)
  const token = ref<string>('')

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