import request from '@/utils/request'
import type { ProjectForm } from '@/types/models'
import axios from 'axios'
import type { Project } from '../types/task'

// 获取项目列表
export function getAllProjects() {
  return request({
    url: '/api/projects',
    method: 'get'
  })
}

// 获取项目详情
export const getProjectDetail = (id: number) => {
  return request({
    url: `/api/projects/${id}`,
    method: 'get'
  })
}

// 创建项目
export function createProject(data: any) {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(projectId: string, data: any) {
  return request({
    url: `/projects/${projectId}`,
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(projectId: string) {
  return request({
    url: `/projects/${projectId}`,
    method: 'delete'
  })
}

// 获取项目成员
export function getProjectMembers(id: number) {
  return request({
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

// 搜索项目成员
export function searchProjectMembers(id: number, params?: {
  keyword?: string
  page?: number
  pageSize?: number
}) {
  return request<{
    code: number
    data: {
      total: number
      items: {
        id: number
        username: string
        email: string
        avatar?: string
        status: number
        createTime: string
      }[]
    }
    message: string
  }>({
    url: `/api/projects/${id}/members/search`,
    method: 'get',
    params
  })
}

/**
 * 上传项目文件
 * @param file 文件对象
 */
export function uploadProjectFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传项目文件
 * @param files 文件列表
 */
export function batchUploadProjectFiles(files: File[]) {
  const formData = new FormData()
  
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request<{
    code: number
    data: string[]
    message: string
  }>({
    url: '/api/files/batch-upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 获取项目附件列表
export function getProjectAttachments(id: number | string) {
  return request({
    url: `/api/projects/${id}/attachments`,
    method: 'get'
  })
}

// 删除项目附件
export function deleteAttachment(projectId: number | string, fileId: number | string) {
  return request({
    url: `/api/projects/${projectId}/attachments/${fileId}`,
    method: 'delete'
  })
} 