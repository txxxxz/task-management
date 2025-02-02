import request from '@/utils/request'
import type { UserInfo, LoginParams, RegisterParams, LoginResponse } from '@/types/user'

// 用户登录
export const login = (data: LoginParams) => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data: RegisterParams) => {
  return request<LoginResponse>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request<UserInfo>({
    url: '/auth/user/info',
    method: 'get'
  })
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
    url: '/user/password',
    method: 'put',
    data
  })
}

// 更新用户信息
export const updateUserInfo = (data: Partial<UserInfo>) => {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
} 