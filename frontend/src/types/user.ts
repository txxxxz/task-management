export interface UserVO {
  id: number
  username: string
  email: string
  phone: string
  avatar?: string
  status: number
  role: number
  createTime: string
  updateTime: string
}

export interface LoginVO {
  token: string
  user: UserVO
}

export interface LoginDTO {
  username: string
  password: string
  role: number
}

export interface RegisterDTO {
  username: string
  password: string
  email: string
  phone: string
  role: number
}

// UserInfo接口用于前端存储和使用用户信息
export interface UserInfo {
  id: string      // 接口可能返回string或number，统一在前端使用string
  username: string
  email: string
  phone: string
  avatar?: string
  status: boolean  // 布尔值表示用户启用状态
  role: number
  createTime: string
  updateTime: string
}

export interface LoginParams {
  username: string
  password: string
  role: number
}

export interface RegisterParams extends LoginParams {
  email: string
  phone: string
}

// 标准响应结构
export interface ApiResponse<T> {
  code: number
  data: T
  msg?: string
  message?: string
}

// 登录响应中的实际数据结构
export interface LoginResponse {
  token: string
  user: UserVO
} 