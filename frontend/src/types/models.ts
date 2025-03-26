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
  status: number; // 0-筹备中，1-进行中，2-已完成，3-已归档
  priority: number; // 1-低，2-中，3-高，4-紧急
  startTime: string;
  endTime: string;
  createTime: string;
  createUser: number;
  updateTime: string;
  updateUser: number;
  members: string[];
  attachments?: string[];
}

export interface ProjectForm {
  name: string;
  description: string;
  startTime: string;
  endTime: string;
  members: string[];
  attachments?: string[];
  status: number;
  priority: number;
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
  parentId?: number | null;
  creator: {
    id: number;
    username: string;
    avatar?: string;
  };
  children?: Comment[];
}

export interface Attachment extends BaseModel {
  filename: string;
  fileUrl: string;
  taskId: number;
  uploaderId: number;
  uploader?: User;
} 