import request from '@/utils/request'
import type { UserInfo, LoginParams, RegisterParams, LoginResponse, ApiResponse } from '@/types/user'

// 用户登录 - 返回完整响应而不是仅data字段
export const login = (data: LoginParams) => {
  return request<ApiResponse<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data: RegisterParams) => {
  return request<ApiResponse<LoginResponse>>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息 - 返回完整响应而不是仅data字段
export const getUserInfo = () => {
  return request<ApiResponse<UserInfo>>({
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
export const getAllUsers = (params?: {
  keyword?: string
  role?: string
  page?: number
  pageSize?: number
}) => {
  return request<{
    code: 1
    data: {
      total: number
      items: {
        id: number
        username: string
        email: string
        avatar?: string
        status: number
        role: number
        createTime: string
      }[]
    }
    msg: string | null
  }>({
    url: '/api/members',
    method: 'get',
    params
  })
}

// 获取所有项目负责人列表
export const getAllLeaders = (params?: {
  page?: number
  pageSize?: number
}) => {
  return request<{
    code: 1
    data: {
      total: number
      items: {
        id: number
        username: string
        email: string
        avatar?: string
        status: number
        role: number
        createTime: string
      }[]
    }
    msg: string | null
  }>({
    url: '/api/members/leaders',
    method: 'get',
    params
  })
}

// 搜索项目负责人
export const searchLeaders = (keyword: string, params?: {
  page?: number
  pageSize?: number
}) => {
  return request<{
    code: 1
    data: {
      total: number
      items: {
        id: number
        username: string
        email: string
        avatar?: string
        status: number
        role: number
        createTime: string
      }[]
    }
    msg: string | null
  }>({
    url: '/api/members/leaders/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
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

// 用户类型定义
export interface UserDetail {
  id: number
  username: string
  email: string
  phone: string
  avatar?: string
  status: number
  role: number
  createTime: string
}

// 分页结果接口
export interface PageResult<T> {
  records: T[]
  total: number
}

// 查询参数接口
export interface UserQuery {
  username?: string
  email?: string
  phone?: string
  role?: number
  status?: number
  page?: number
  pageSize?: number
}

// 用户表单数据
export interface UserForm {
  username: string
  email: string
  phone?: string
  password?: string
  role: number
  status: number
}

// 用户注册数据
export interface UserRegisterData {
  username: string
  password: string
  email: string
  phone?: string
}

/**
 * 获取用户列表
 * @param params 查询参数
 */
export function getUserList(params: UserQuery) {
  return request({
    url: '/auth/users',
    method: 'get',
    params
  }).then(res => res.data)
}

/**
 * 获取用户详情
 * @param id 用户ID
 */
export function getUserById(id: number) {
  return request({
    url: `/auth/users/${id}`,
    method: 'get'
  }).then(res => res.data)
}

/**
 * 创建用户
 * @param data 用户数据
 */
export function createUser(data: UserRegisterData) {
  return request({
    url: '/auth/users',
    method: 'post',
    data
  }).then(res => res.data)
}

/**
 * 更新用户
 * @param id 用户ID
 * @param data 用户数据
 */
export function updateUser(id: number, data: Partial<UserForm>) {
  return request({
    url: `/auth/users/${id}`,
    method: 'put',
    data
  }).then(res => res.data)
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function deleteUser(id: number) {
  return request({
    url: `/auth/users/${id}`,
    method: 'delete'
  }).then(res => res.data)
}

/**
 * 更新用户状态
 * @param id 用户ID
 * @param status 状态值 0-禁用 1-启用
 */
export function updateUserStatusById(id: number, status: number) {
  return request({
    url: `/auth/users/${id}/status`,
    method: 'patch',
    data: { status }
  }).then(res => res.data)
}
