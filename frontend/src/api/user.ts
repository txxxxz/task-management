import request from '@/utils/request'
import type { User } from '@/types/models'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams extends LoginParams {
  email: string
}

export const login = (data: LoginParams) => {
  return request.post<string>('/user/login', data)
}

export const register = (data: RegisterParams) => {
  return request.post<void>('/user/register', data)
}

export const getUserInfo = () => {
  return request.get<User>('/user/info')
}

export const updateUserInfo = (data: Partial<User>) => {
  return request.put<void>('/user/info', data)
}

export const updatePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.put<void>('/user/password', data)
} 