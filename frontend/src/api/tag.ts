import request from '@/utils/request'
import type { Tag } from '@/types/tag'

// 标签接口
export interface Tag {
  id: string
  name: string
  color: string
  projectId?: string
}

// 获取标签列表
export function getTagList(params?: {
  keyword?: string
  projectId?: string
  page?: number
  pageSize?: number
}) {
  return request<{
    code: number
    data: {
      total: number
      items: Tag[]
    }
    message: string
  }>({
    url: '/api/tags',
    method: 'get',
    params
  })
}

// 创建标签
export function createTag(data: Partial<Tag>) {
  return request<{
    code: number
    data: Tag
    message: string
  }>({
    url: '/api/tags',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(id: number, data: Partial<Tag>) {
  return request<{
    code: number
    data: Tag
    message: string
  }>({
    url: `/api/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(id: number) {
  return request<{
    code: number
    data: null
    message: string
  }>({
    url: `/api/tags/${id}`,
    method: 'delete'
  })
}

// 根据ID获取标签
export function getTagById(id: number) {
  return request<{
    code: number
    data: Tag
    message: string
  }>({
    url: `/api/tags/${id}`,
    method: 'get'
  })
}

// 根据项目ID获取标签列表
export function getTagsByProjectId(projectId: number) {
  return request<{
    code: number
    data: Tag[]
    message: string
  }>({
    url: `/api/tags/project/${projectId}`,
    method: 'get'
  })
}

// 搜索标签
export function searchTags(keyword: string, projectId?: number) {
  return request<{
    code: number
    data: Tag[]
    message: string
  }>({
    url: '/api/tags/search',
    method: 'get',
    params: { keyword, projectId }
  })
} 