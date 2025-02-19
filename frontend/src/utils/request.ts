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
    // 不需要token的白名单路径
    const whiteList = ['/auth/login', '/auth/register', '/auth/user/avatar']
    const isWhitelisted = whiteList.some(path => config.url?.includes(path))
    
    // 如果不在白名单中，则添加token
    if (!isWhitelisted) {
      const token = localStorage.getItem('token')
      if (token && config.headers) {
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
    const { data } = response
    
    // 处理url中的/api前缀
    if (response.config.url) {
      response.config.url = response.config.url.replace('/api', '')
    }
    
    // 如果响应成功，直接返回数据
    if (data.code === 1) {
      return data.data
    }
    
    // 处理业务错误
    const errorMsg = data.msg || '操作失败'
    console.error('业务错误:', errorMsg)
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  },
  (error) => {
    // 处理HTTP错误
    let errorMsg = '网络错误，请稍后重试'
    
    if (error.response) {
      const { status, data } = error.response
      errorMsg = data.msg || data.message || '请求失败'
      
      if (status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
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