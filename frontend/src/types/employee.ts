export interface Employee {
  id: number
  name: string
  email: string
  phone: string
  role: 'Admin' | 'Leader' | 'Member'
  status: boolean
  createdAt: string
}

export interface EmployeeForm {
  name: string
  email: string
  phone: string
  role: 'Admin' | 'Leader' | 'Member'
  status: boolean
}

export interface EmployeeQuery {
  name?: string
  email?: string
  role?: string
  status?: boolean
  page?: number
  pageSize?: number
}

export interface EmployeeResponse {
  total: number
  items: Employee[]
} 