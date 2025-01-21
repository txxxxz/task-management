import request from '@/utils/request'
import type { Project } from '@/types/models'

export const getProjects = () => {
  return request.get<Project[]>('/projects')
}

export const getProjectById = (id: number) => {
  return request.get<Project>(`/projects/${id}`)
}

export const createProject = (data: Partial<Project>) => {
  return request.post<Project>('/projects', data)
}

export const updateProject = (id: number, data: Partial<Project>) => {
  return request.put<void>(`/projects/${id}`, data)
}

export const deleteProject = (id: number) => {
  return request.delete<void>(`/projects/${id}`)
} 