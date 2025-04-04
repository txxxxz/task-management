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
      if (response.data && response.data.data && response.data.data.token) {
        token.value = response.data.data.token
        localStorage.setItem('token', response.data.data.token)
      } else {
        console.error('Token not found in response', response)
        return false
      }
      await getUserInfo()
      return true
    } catch (error) {
      return false
    }
  }

  const getUserInfo = async () => {
    try {
      const response = await userApi.getUserInfo()
      if (response.data && response.data.data) {
        const userData = response.data.data
        user.value = {
          id: Number(userData.id),
          username: userData.username,
          email: userData.email,
          phone: userData.phone || '',
          avatar: userData.avatar,
          status: Number(userData.status),
          role: Number(userData.role),
          createTime: userData.createTime,
          updateTime: userData.updateTime
        }
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