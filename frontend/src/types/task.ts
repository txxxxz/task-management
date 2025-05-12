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
  number?: string
  name?: string
  title?: string
  description: string
  createTime: string
  dueTime?: string
  deadline?: string  // 后端返回的截止时间字段
  startTime?: string
  priority: number // 1-低，2-中，3-高，4-紧急
  status: number // 0-待处理，1-进行中，2-已完成，3-已取消
  members: string[]
  tags: string[]
  files?: TaskFile[]
  comments?: TaskComment[]
  projectId: string
  projectName?: string
  attachments?: string[]
  commentCount?: number // 评论数量
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
  taskId: number
  content: string
  parentId?: number | null
  createTime: string
  createUser: number | string
  createUserName: string
  createUserAvatar: string
  children?: TaskComment[]
}

// 创建任务参数
export interface CreateTaskParams {
  name: string
  description: string
  dueTime: string // ISO格式：YYYY-MM-DDTHH:mm:ss (原deadline字段)
  startTime?: string // ISO格式：YYYY-MM-DDTHH:mm:ss
  priority: number // 1-低，2-中，3-高，4-紧急
  members: string[]
  tagIds: string[] | number[]
  projectId: string
  attachments?: string[]
}

// 更新任务参数
export interface UpdateTaskParams extends Partial<CreateTaskParams> {
  status?: number // 0-待处理，1-进行中，2-已完成，3-已取消
  projectId?: string
  tagIds?: string[] | number[]
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
  status?: number // 0-待处理，1-进行中，2-已完成，3-已取消
  priority?: number // 1-低，2-中，3-高，4-紧急
  projectId?: string
  startTime?: string
  endTime?: string
  dueStartTime?: string // 截止时间开始
  dueEndTime?: string  // 截止时间结束
  member?: string // 成员用户名
  tags?: string   // 标签IDs，逗号分隔
  page?: number
  pageSize?: number
} 