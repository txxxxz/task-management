import request from '@/utils/request'
import type { UserInfo, LoginParams, RegisterParams, LoginResponse } from '@/types/user'

// 用户登录 - 明确返回LoginResponse而不是AxiosResponse
export const login = (data: LoginParams) => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data
  }).then(response => response.data)
}

// 用户注册
export const register = (data: RegisterParams) => {
  return request<LoginResponse>({
    url: '/auth/register',
    method: 'post',
    data
  }) as Promise<LoginResponse>
}

// 获取用户信息 - 明确返回UserInfo而不是AxiosResponse
export const getUserInfo = () => {
  return request<UserInfo>({
    url: '/auth/user/info',
    method: 'get'
  }) as Promise<UserInfo> // 显式指定返回Promise<UserInfo>
}

// 检查用户名是否存在
export const checkUsername = (username: string) => {
  return request.get<boolean>(`/auth/check/${username}`)
}

// 登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request({
    url: '/auth/user/password',
    method: 'put',
    data
  })
}

// 更新用户信息
export const updateUserInfo = (data: Partial<UserInfo>) => {
  return request({
    url: '/auth/user/info',
    method: 'put',
    data
  })
}

interface BatchRegisterData {
  users: {
    username: string
    email: string
    password: string
    role: string
  }[]
}

// 获取所有用户列表
export const getAllUsers = () => {
  return request({
    url: '/api/users',
    method: 'get'
  })
}

// 批量注册用户
export const batchRegisterUsers = (data: BatchRegisterData) => {
  return request({
    url: '/api/users/batch-register',
    method: 'post',
    data
  })
}

// 更新用户状态（启用/禁用）
export const updateUserStatus = (userId: string, status: boolean) => {
  return request({
    url: `/api/users/${userId}/status`,
    method: 'put',
    data: { status }
  })
}

// 更新用户角色
export const updateUserRole = (userId: string, role: string) => {
  return request({
    url: `/api/users/${userId}/role`,
    method: 'put',
    data: { role }
  })
}

// 获取用户详细信息
export const getUserDetails = (userId: string) => {
  return request({
    url: `/api/users/${userId}`,
    method: 'get'
  })
}