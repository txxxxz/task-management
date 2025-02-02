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

export interface UserInfo {
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

export interface LoginParams {
  username: string
  password: string
  role: number
}

export interface RegisterParams extends LoginParams {
  email: string
  phone: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
} 