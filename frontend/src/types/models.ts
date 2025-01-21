export interface BaseModel {
  id: number;
  createTime: string;
  updateTime: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  avatar?: string;
  status: number;
  createTime: string;
  createUser: number;
  updateTime: string;
  updateUser: number;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  status: number;
  priority: number;
  startTime: string;
  endTime: string;
  createTime: string;
  createUser: number;
  updateTime: string;
  updateUser: number;
}

export interface Task {
  id: number;
  projectId: number;
  name: string;
  description: string;
  status: number;
  priority: number;
  startTime: string;
  endTime: string;
  assignee: number;
  createTime: string;
  createUser: number;
  updateTime: string;
  updateUser: number;
}

export interface Tag extends BaseModel {
  name: string;
  color: string;
}

export interface Comment extends BaseModel {
  content: string;
  taskId: number;
  userId: number;
  user?: User;
}

export interface Attachment extends BaseModel {
  filename: string;
  fileUrl: string;
  taskId: number;
  uploaderId: number;
  uploader?: User;
} 