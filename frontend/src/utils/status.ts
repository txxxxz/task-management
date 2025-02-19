// 项目状态类型
export type ProjectStatus = 0 | 1 | 2 | 3
export type ProjectPriority = 1 | 2 | 3 | 4

// 获取项目状态类型
export function getStatusType(status: ProjectStatus): 'success' | 'warning' | 'info' | 'primary' | 'danger' {
  const statusMap = {
    0: 'info',      // 筹备中
    1: 'primary',   // 进行中
    2: 'success',   // 已完成
    3: 'warning'    // 已归档
  } as const
  return statusMap[status]
}

// 获取项目状态文本
export function getStatusText(status: ProjectStatus): string {
  const statusMap = {
    0: '筹备中',
    1: '进行中',
    2: '已完成',
    3: '已归档'
  }
  return statusMap[status]
}

// 获取优先级类型
export function getPriorityType(priority: ProjectPriority): 'success' | 'warning' | 'info' | 'primary' | 'danger' {
  const priorityMap = {
    1: 'info',      // 低
    2: 'primary',   // 中
    3: 'warning',   // 高
    4: 'danger'     // 紧急
  } as const
  return priorityMap[priority]
}

// 获取优先级文本
export function getPriorityText(priority: ProjectPriority): string {
  const priorityMap = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  }
  return priorityMap[priority]
}

// 获取优先级选项
export function getPriorityOptions() {
  return [
    { value: 1, label: '低' },
    { value: 2, label: '中' },
    { value: 3, label: '高' },
    { value: 4, label: '紧急' }
  ]
}

// 获取状态选项
export function getStatusOptions() {
  return [
    { value: 0, label: '筹备中' },
    { value: 1, label: '进行中' },
    { value: 2, label: '已完成' },
    { value: 3, label: '已归档' }
  ]
} 