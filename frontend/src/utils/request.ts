import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// request interceptor
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    console.log('sending request:', config.url, config.data || config.params)
    
    // white list of paths that do not need token 
    const whiteList = ['/auth/login', '/auth/register', '/auth/check']
    const isWhitelisted = whiteList.some(path => config.url === path)
    
    // add token to all non-whitelisted requests
    if (!isWhitelisted) {
      const token = localStorage.getItem('token')
      console.log('current token:', token)
      
      if (token && config.headers) {
        // add standard Authorization header
        config.headers['Authorization'] = `Bearer ${token}`
        config.headers['token'] = token
      } else {
        console.warn('no token or headers not found, request may fail:', config.url)
      }
    }

    // 处理文件上传请求
    if (config.headers && config.headers['Content-Type'] === 'multipart/form-data') {
      // 对于文件上传请求，不要修改 Content-Type，让浏览器自动设置正确的 boundary
      delete config.headers['Content-Type']
    }

    // get请求参数处理
    if (config.method === 'get' && config.params) {
      // 不再手动拼接URL，让axios自动处理参数
      // 只需要清理undefined和null值
      const cleanParams: Record<string, any> = {};
      
      for (const key in config.params) {
        const value = config.params[key];
        if (value !== null && value !== undefined) {
          cleanParams[key] = value;
        }
      }
      
      config.params = cleanParams;
    }

    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data, config } = response
    
    console.log('收到API响应:', config.url, data)
    
    // 不再自动处理标准响应格式，直接返回完整响应
    return response
  },
  (error) => {
    // 处理HTTP错误
    let errorMsg = '网络错误，请稍后重试'
    
    if (error.response) {
      const { status, data } = error.response
      console.error(`HTTP错误 ${status}:`, data)
      
      // 处理标准响应格式
      if (data && typeof data === 'object' && 'code' in data && data.msg) {
        errorMsg = data.msg
      } else {
        errorMsg = data?.message || `请求失败 (${status})`
      }
      
      // 处理特定状态码
      if (status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        errorMsg = 'Login expired, please log in again'
      } else if (status === 403) {
        errorMsg = 'No permission to access this resource'
      } else if (status === 500) {
        errorMsg = 'Server error, please contact the administrator'
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('未收到响应:', error.request)
      errorMsg = 'Server did not respond, please check your network connection'
    } else {
      // 请求配置错误
      console.error('请求配置错误:', error.message)
    }
    
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  }
)

export default service 