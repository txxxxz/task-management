import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { login as userLogin, getUserInfo, logout as userLogout } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  const login = async (username: string, password: string, role: number) => {
    try {
      loading.value = true
      const data = await userLogin({ username, password, role })
      setToken(data.token)
      setUserInfo(data.user)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (error: any) {
      console.error('Login failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchUserInfo = async () => {
    try {
      const data = await getUserInfo()
      setUserInfo(data)
      return data
    } catch (error: any) {
      console.error('Fetch user info failed:', error)
      throw error
    }
  }

  const logout = async () => {
    try {
      await userLogout()
      clearUserInfo()
      router.push('/login')
      ElMessage.success('退出登录成功')
    } catch (error: any) {
      console.error('Logout failed:', error)
      throw error
    }
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  const clearUserInfo = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    loading,
    login,
    logout,
    fetchUserInfo,
    setToken,
    setUserInfo,
    clearUserInfo
  }
}) 