import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    console.log('发送请求:', config.url, config.data || config.params)
    
    // 不需要token的白名单路径
    const whiteList = ['/auth/login', '/auth/register', '/auth/user/avatar']
    const isWhitelisted = whiteList.some(path => config.url?.includes(path))
    
    // 如果不在白名单中，则添加token
    if (!isWhitelisted) {
      const token = localStorage.getItem('token')
      if (token && config.headers) {
        config.headers['Authorization'] = `Bearer ${token}`
        config.headers['token'] = token
      }
    }

    // get请求参数处理
    if (config.method === 'get' && config.params) {
      let url = config.url + '?';
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName];
        if (value !== null && typeof value !== 'undefined') {
          url += `${encodeURIComponent(propName)}=${encodeURIComponent(value)}&`;
        }
      }
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
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
    
    // 处理url中的/api前缀
    if (response.config.url) {
      response.config.url = response.config.url.replace('/api', '')
    }
    
    // 处理不同格式的响应数据
    // 如果响应成功且有标准格式（带code字段）
    if (data && typeof data === 'object') {
      if ('code' in data) {
        if (data.code === 1 || data.code === 200 || data.code === 0) {
          console.log('返回的数据:', data.data)
          return data.data
        } else {
          // 处理业务错误
          const errorMsg = data.msg || data.message || '操作失败'
          console.error('业务错误:', errorMsg, data)
          ElMessage.error(errorMsg)
          return Promise.reject(new Error(errorMsg))
        }
      }
      // 无code字段，直接返回响应数据
      console.log('直接返回响应数据:', data)
      return data
    }
    
    // 最后兜底，返回原始响应
    return response
  },
  (error) => {
    // 处理HTTP错误
    let errorMsg = '网络错误，请稍后重试'
    
    if (error.response) {
      const { status, data } = error.response
      console.error(`HTTP错误 ${status}:`, data)
      errorMsg = data?.msg || data?.message || `请求失败 (${status})`
      
      // 处理特定状态码
      if (status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        errorMsg = '登录已过期，请重新登录'
      } else if (status === 403) {
        errorMsg = '没有权限访问该资源'
      } else if (status === 500) {
        errorMsg = '服务器错误，请联系管理员'
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('未收到响应:', error.request)
      errorMsg = '服务器未响应，请检查网络连接'
    } else {
      // 请求配置错误
      console.error('请求配置错误:', error.message)
    }
    
    // 处理url中的/api前缀
    if (error.config?.url) {
      error.config.url = error.config.url.replace('/api', '')
    }
    
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  }
)

export default service 