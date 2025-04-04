import request from '@/utils/request'
import type { Tag } from '@/types/tag'

// 标签接口
// 下面的定义与导入的类型冲突，删除此定义
// 通用API响应格式
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

// 通用分页响应格式
interface PagedResponse<T> {
  total: number
  items: T[]
}

// 获取标签列表
export function getTagList(params?: {
  keyword?: string
  projectId?: string
  page?: number
  pageSize?: number
}) {
  return request<ApiResponse<PagedResponse<Tag>>>({
    url: '/api/tags',
    method: 'get',
    params
  })
}

// 创建标签
export function createTag(data: Partial<Tag>) {
  return request<ApiResponse<Tag>>({
    url: '/api/tags',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(id: number, data: Partial<Tag>) {
  return request<ApiResponse<Tag>>({
    url: `/api/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(id: number) {
  return request<ApiResponse<null>>({
    url: `/api/tags/${id}`,
    method: 'delete'
  })
}

// 根据ID获取标签
export function getTagById(id: number) {
  return request<ApiResponse<Tag>>({
    url: `/api/tags/${id}`,
    method: 'get'
  })
}

// 根据项目ID获取标签列表
export function getTagsByProjectId(projectId: number) {
  return request<ApiResponse<PagedResponse<Tag>>>({
    url: `/api/tags/project/${projectId}`,
    method: 'get'
  })
}

// 搜索标签
export function searchTags(keyword: string, projectId?: number) {
  return request<ApiResponse<PagedResponse<Tag>>>({
    url: '/api/tags/search',
    method: 'get',
    params: { keyword, projectId }
  })
}

// 获取所有标签
export function getAllTags() {
  return request<ApiResponse<PagedResponse<Tag>>>({
    url: '/api/tags/all',
    method: 'get'
  })
} 