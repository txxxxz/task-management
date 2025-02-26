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
      // 注意：由于响应拦截器直接返回data.data，所以response就是API的实际数据
      const response = await userLogin({ username, password, role })
      
      console.log('登录响应:', response)
      
      // 检查响应数据是否完整
      if (response) {
        if (response.token) {
          setToken(response.token)
          
          // 检查是否有用户信息
          if (response.user) {
            // 转换用户数据
            const userData = {
              ...response.user,
              id: String(response.user.id),
              status: response.user.status === 1 || response.user.status === true
            }
            setUserInfo(userData)
          }
          
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          // 如果response没有token，抛出错误
          console.error('登录响应缺少token:', response)
          throw new Error('登录响应数据格式不正确')
        }
      } else {
        // 如果response为空，抛出错误
        console.error('登录响应为空')
        throw new Error('登录响应为空')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请检查网络或凭据')
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchUserInfo = async () => {
    try {
      // 注意：由于响应拦截器直接返回data.data，所以response就是用户信息数据本身
      const userData = await getUserInfo()
      
      console.log('获取用户信息响应:', userData)
      
      if (userData) {
        // 确保用户数据格式一致
        const formattedUserData = {
          ...userData,
          id: String(userData.id), // 确保ID是字符串类型
          status: typeof userData.status === 'boolean' 
            ? userData.status 
            : userData.status === 1
        }
        
        setUserInfo(formattedUserData)
        return formattedUserData
      } else {
        console.error('获取用户信息失败：响应数据为空')
        throw new Error('获取用户信息失败：响应数据为空')
      }
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      ElMessage.error(error.message || '获取用户信息失败')
      throw error
    }
  }

  const logout = async () => {
    try {
      await userLogout()
    } catch (error: any) {
      console.error('退出登录失败:', error)
    } finally {
      clearUserInfo()
      router.push('/login')
      ElMessage.success('退出登录成功')
    }
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    // 可选：将一些常用信息存储在localStorage中
    localStorage.setItem('userRole', String(info.role))
    localStorage.setItem('username', info.username)
  }

  const clearUserInfo = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('username')
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