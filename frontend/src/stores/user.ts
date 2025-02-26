import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/user'
import { login as userLogin, getUserInfo, logout as userLogout } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { AxiosResponse } from 'axios'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  const login = async (username: string, password: string, role: number) => {
    try {
      loading.value = true
      // 调用登录API
      const responseData = await userLogin({ username, password, role })
      
      console.log('登录API返回:', responseData)
      
      // 响应拦截器已处理了response.data，responseData直接就是登录数据
      if (responseData && responseData.token) {
        // 设置token
        setToken(responseData.token)
        
        // 如果有用户信息，设置用户信息
        if (responseData.user) {
          // 转换用户数据格式
          const userData: UserInfo = {
            id: String(responseData.user.id || ''),
            username: responseData.user.username || '',
            email: responseData.user.email || '',
            phone: responseData.user.phone || '',
            avatar: responseData.user.avatar,
            status: responseData.user.status === 1 || responseData.user.status === true,
            role: Number(responseData.user.role || 0),
            createTime: responseData.user.createTime || '',
            updateTime: responseData.user.updateTime || ''
          }
          setUserInfo(userData)
        }
        
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } else {
        // 登录响应缺少token
        console.error('登录响应无效:', responseData)
        throw new Error('登录响应数据格式不正确')
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
      // 调用获取用户信息API
      const userData = await getUserInfo()
      
      console.log('获取用户信息返回:', userData)
      
      if (userData) {
        // 确保用户数据格式一致
        const formattedUserData: UserInfo = {
          id: String(userData.id || ''),
          username: userData.username || '',
          email: userData.email || '',
          phone: userData.phone || '',
          avatar: userData.avatar,
          status: typeof userData.status === 'boolean' 
            ? userData.status 
            : userData.status === 1,
          role: Number(userData.role || 0),
          createTime: userData.createTime || '',
          updateTime: userData.updateTime || ''
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