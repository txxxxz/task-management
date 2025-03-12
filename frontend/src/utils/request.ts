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

    // 处理文件上传请求
    if (config.headers && config.headers['Content-Type'] === 'multipart/form-data') {
      // 对于文件上传请求，不要修改 Content-Type，让浏览器自动设置正确的 boundary
      delete config.headers['Content-Type']
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