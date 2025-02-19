// 项目接口
export interface Project {
  id: number
  name: string
  description: string
  status: number // 0-筹备中，1-进行中，2-已完成，3-已归档
  priority: number // 1-低，2-中，3-高，4-紧急
  startTime: string
  endTime: string
  createTime: string
  createUser: number
  updateTime: string
  updateUser: number
  members: string[]
  attachments?: string[]
}

// 任务详情接口
export interface TaskDetail {
  id: string
  number: string
  name: string
  description: string
  createTime: string
  dueTime: string
  priority: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'TODO' | 'IN_PROGRESS' | 'DONE'
  members: string[]
  tags: string[]
  files: TaskFile[]
  comments: TaskComment[]
  projectId: string
  projectName: string
}

// 任务文件接口
export interface TaskFile {
  id: string
  name: string
  url: string
  size: number
  uploader: string
  uploadTime: string
}

// 任务评论接口
export interface TaskComment {
  id: number
  username: string
  avatar: string
  content: string
  createTime: string
  replyTo?: string
}

// 创建任务参数
export interface CreateTaskParams {
  name: string
  description: string
  dueTime: string
  priority: TaskDetail['priority']
  members: string[]
  tags: string[]
  projectId: string
}

// 更新任务参数
export interface UpdateTaskParams extends Partial<CreateTaskParams> {
  status?: TaskDetail['status']
  projectId?: string
}

// 评论参数
export interface CommentParams {
  taskId: string
  content: string
  replyTo?: string
}

// 任务查询参数
export interface TaskQueryParams {
  keyword?: string
  status?: TaskDetail['status']
  priority?: TaskDetail['priority']
  projectId?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
} 