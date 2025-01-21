import { defineStore } from 'pinia'
import { ref } from 'vue'
import { UserInfo, login as userLogin, getUserInfo, logout as userLogout } from '../api/user'
import { ElMessage } from 'element-plus'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  const login = async (username: string, password: string) => {
    try {
      loading.value = true
      const res = await userLogin({ username, password })
      token.value = res.token
      localStorage.setItem('token', res.token)
      await fetchUserInfo()
      router.push('/')
      ElMessage.success('登录成功')
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res
    } catch (error) {
      console.error('Fetch user info failed:', error)
      throw error
    }
  }

  const logout = async () => {
    try {
      await userLogout()
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.success('退出登录成功')
    } catch (error) {
      console.error('Logout failed:', error)
      throw error
    }
  }

  return {
    token,
    userInfo,
    loading,
    login,
    logout,
    fetchUserInfo
  }
}) 