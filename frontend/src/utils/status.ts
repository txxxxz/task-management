export const getStatusType = (status: number) => {
  const types: Record<number, 'success' | 'warning' | 'info' | 'primary' | 'danger'> = {
    0: 'primary',  // 未开始
    1: 'info',     // 进行中
    2: 'success',  // 已完成
    3: 'warning',  // 已暂停
    4: 'danger'    // 已取消
  }
  return types[status] || 'info'
}

export const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已完成',
    3: '已暂停',
    4: '已取消'
  }
  return texts[status] || '未知'
}

export const getPriorityType = (priority: number) => {
  const types: Record<number, 'success' | 'warning' | 'info' | 'danger'> = {
    0: 'info',     // 低
    1: 'success',  // 中
    2: 'warning',  // 高
    3: 'danger'    // 紧急
  }
  return types[priority] || 'info'
}

export const getPriorityText = (priority: number) => {
  const texts: Record<number, string> = {
    0: '低',
    1: '中',
    2: '高',
    3: '紧急'
  }
  return texts[priority] || '未知'
} 