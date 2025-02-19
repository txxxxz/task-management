import request from '@/utils/request'
import type { Project, ProjectForm } from '@/types/models'

// 获取项目列表
export function getProjectList(params?: {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}) {
  return request<{
    code: number
    data: {
      total: number
      items: Project[]
    }
    message: string
  }>({
    url: '/api/projects',
    method: 'get',
    params
  })
}

// 获取项目详情
export function getProjectDetail(id: number) {
  return request<{
    code: number
    data: Project
    message: string
  }>({
    url: `/api/projects/${id}`,
    method: 'get'
  })
}

// 创建项目
export function createProject(data: ProjectForm) {
  return request<{
    code: number
    data: Project
    message: string
  }>({
    url: '/api/projects',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(id: number, data: Partial<ProjectForm>) {
  return request<{
    code: number
    data: Project
    message: string
  }>({
    url: `/api/projects/${id}`,
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id: number) {
  return request<{
    code: number
    message: string
  }>({
    url: `/api/projects/${id}`,
    method: 'delete'
  })
}

// 获取项目成员
export function getProjectMembers(id: number) {
  return request<{
    code: number
    data: string[]
    message: string
  }>({
    url: `/api/projects/${id}/members`,
    method: 'get'
  })
}

// 添加项目成员
export function addProjectMember(id: number, username: string) {
  return request<{
    code: number
    message: string
  }>({
    url: `/api/projects/${id}/members`,
    method: 'post',
    data: { username }
  })
}

// 移除项目成员
export function removeProjectMember(id: number, username: string) {
  return request<{
    code: number
    message: string
  }>({
    url: `/api/projects/${id}/members/${username}`,
    method: 'delete'
  })
} 