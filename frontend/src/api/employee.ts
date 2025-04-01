import request from '@/utils/request'
// import type { Employee, EmployeeForm, EmployeeQuery, EmployeeResponse } from '@/types/employee'

// 定义员工相关接口
export interface Employee {
  id: number
  name: string
  email: string
  phone: string
  role: 'Admin' | 'Leader' | 'Member'
  status: boolean
  createdAt: string
}

export interface EmployeeForm {
  name: string
  email: string
  phone: string
  role: 'Admin' | 'Leader' | 'Member'
  status: boolean
}

export interface EmployeeQuery {
  name?: string
  email?: string
  role?: string
  status?: boolean
  page?: number
  pageSize?: number
}

export interface EmployeeResponse {
  total: number
  items: Employee[]
}

// 获取员工列表
export function getEmployeeList(params: EmployeeQuery) {
  return request<EmployeeResponse>({
    url: '/api/employees',
    method: 'get',
    params
  })
}

// 获取员工详情
export function getEmployeeById(id: number) {
  return request<Employee>({
    url: `/api/employees/${id}`,
    method: 'get'
  })
}

// 创建员工
export function createEmployee(data: EmployeeForm) {
  return request<Employee>({
    url: '/api/employees',
    method: 'post',
    data
  })
}

// 更新员工信息
export function updateEmployee(id: number, data: Partial<EmployeeForm>) {
  return request<Employee>({
    url: `/api/employees/${id}`,
    method: 'put',
    data
  })
}

// 删除员工
export function deleteEmployee(id: number) {
  return request<void>({
    url: `/api/employees/${id}`,
    method: 'delete'
  })
}

// 更新员工状态
export function toggleEmployeeStatus(id: number, status: boolean) {
  return request<Employee>({
    url: `/api/employees/${id}/status`,
    method: 'patch',
    data: { status }
  })
} 