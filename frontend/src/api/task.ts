import request from '@/utils/request'
import type { Task, Comment, Attachment } from '@/types/models'

export const getTasks = (projectId: number) => {
  return request.get<Task[]>(`/projects/${projectId}/tasks`)
}

export const getTaskById = (projectId: number, taskId: number) => {
  return request.get<Task>(`/projects/${projectId}/tasks/${taskId}`)
}

export const createTask = (projectId: number, data: Partial<Task>) => {
  return request.post<Task>(`/projects/${projectId}/tasks`, data)
}

export const updateTask = (projectId: number, taskId: number, data: Partial<Task>) => {
  return request.put<void>(`/projects/${projectId}/tasks/${taskId}`, data)
}

export const deleteTask = (projectId: number, taskId: number) => {
  return request.delete<void>(`/projects/${projectId}/tasks/${taskId}`)
}

// 评论相关
export const getTaskComments = (taskId: number) => {
  return request.get<Comment[]>(`/tasks/${taskId}/comments`)
}

export const createComment = (taskId: number, content: string) => {
  return request.post<Comment>(`/tasks/${taskId}/comments`, { content })
}

export const deleteComment = (taskId: number, commentId: number) => {
  return request.delete<void>(`/tasks/${taskId}/comments/${commentId}`)
}

// 附件相关
export const getTaskAttachments = (taskId: number) => {
  return request.get<Attachment[]>(`/tasks/${taskId}/attachments`)
}

export const uploadAttachment = (taskId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Attachment>(`/tasks/${taskId}/attachments`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deleteAttachment = (taskId: number, attachmentId: number) => {
  return request.delete<void>(`/tasks/${taskId}/attachments/${attachmentId}`)
} 