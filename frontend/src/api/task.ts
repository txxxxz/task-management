import request from '@/utils/request'
import type { Task, Comment, Attachment } from '@/types/models'
import type { TaskDetail, CreateTaskParams, UpdateTaskParams, TaskQueryParams } from '@/types/task'

// 获取任务列表
export function getTaskList(params?: TaskQueryParams) {
  return request({
    url: '/api/tasks',
    method: 'get',
    params
  })
}

// 获取项目下的任务列表
export function getProjectTasks(projectId: string, params?: Omit<TaskQueryParams, 'projectId'>) {
  return request<{
    total: number
    items: TaskDetail[]
  }>({
    url: '/api/projects/' + projectId + '/tasks',
    method: 'get',
    params
  })
}

// 创建任务
export function createTask(data: CreateTaskParams) {
  return request<TaskDetail>({
    url: '/api/tasks',
    method: 'post',
    data
  })
}

// 在项目下创建任务
export function createProjectTask(projectId: string, data: Omit<CreateTaskParams, 'projectId'>) {
  return request<TaskDetail>({
    url: '/api/projects/' + projectId + '/tasks',
    method: 'post',
    data
  })
}

// 更新任务
export function updateTask(id: string, data: UpdateTaskParams) {
  return request<TaskDetail>({
    url: '/api/tasks/' + id,
    method: 'put',
    data,
    timeout: 10000
  })
}

// 删除任务
export function deleteTask(id: string) {
  return request<void>({
    url: '/api/tasks/' + id,
    method: 'delete'
  })
}

// 获取任务详情
export function getTaskDetail(id: string) {
  return request<TaskDetail>({
    url: '/api/tasks/' + id,
    method: 'get'
  })
}

// 获取任务统计信息
export function getTaskStats(projectId?: string) {
  return request<{
    total: number
    pending: number
    inProgress: number
    completed: number
  }>({
    url: '/api/tasks/stats',
    method: 'get',
    params: { projectId }
  })
}

// 获取当前用户任务统计信息
export function getCurrentUserTaskStats() {
  return request<{
    total: number
    pending: number
    inProgress: number
    completed: number
    todayExpired: number
  }>({
    url: '/api/tasks/stats/user/stats',
    method: 'get'
  })
}

// 获取当前用户指定状态的任务
export function getUserTasksByStatus(status: number, page = 1, pageSize = 10) {
  return request<{
    total: number
    items: TaskDetail[]
  }>({
    url: `/api/tasks/stats/user/status/${status}`,
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取当前用户所有任务
export function getAllUserTasks(page = 1, pageSize = 10) {
  return request<{
    total: number
    items: TaskDetail[]
  }>({
    url: '/api/tasks/stats/user/all',
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取当前用户今日到期的任务
export function getUserTodayExpiredTasks(page = 1, pageSize = 10) {
  return request<{
    total: number
    items: TaskDetail[]
  }>({
    url: '/api/tasks/stats/user/today-expired',
    method: 'get',
    params: { page, pageSize }
  })
}

// 评论相关
export function getTaskComments(taskId: number) {
  return request<Comment[]>({
    url: '/api/tasks/' + taskId + '/comments',
    method: 'get'
  })
}

/**
 * 添加评论
 * @param taskId 任务ID
 * @param commentData 评论数据
 * @returns 评论
 */
export function createComment(taskId: number, commentData: any) {
  return request({
    url: `/api/tasks/${taskId}/comments`,
    method: 'post',
    data: commentData
  })
}

export function deleteComment(taskId: number, commentId: number) {
  return request<void>({
    url: '/api/tasks/' + taskId + '/comments/' + commentId,
    method: 'delete'
  })
}

// 附件相关
export function getTaskAttachments(taskId: number) {
  return request<Attachment[]>({
    url: '/api/tasks/' + taskId + '/attachments',
    method: 'get'
  })
}

export function uploadAttachment(taskId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request<Attachment>({
    url: '/api/tasks/' + taskId + '/attachments',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function deleteAttachment(taskId: number, attachmentId: number) {
  return request<void>({
    url: '/api/tasks/' + taskId + '/attachments/' + attachmentId,
    method: 'delete'
  })
}

/**
 * 获取任务优先级分布数据
 */
export function getTaskPriorityDistribution() {
  return request({
    url: '/api/tasks/stats/priorityDistribution',
    method: 'get'
  })
}

/**
 * 获取任务状态统计数据
 * @param weekOffset 周偏移量
 */
export function getTaskStatusStatsByDay(weekOffset: number) {
  return request({
    url: '/api/tasks/stats/statusStatsByDay',
    method: 'get',
    params: { weekOffset }
  })
} 