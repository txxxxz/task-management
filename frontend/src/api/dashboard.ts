import request from '@/utils/request'

// 获取任务统计数据
export function getTaskStatistics() {
  return request({
    url: '/api/dashboard/statistics',
    method: 'get'
  })
}

// 获取任务趋势数据
export function getTaskTrend(params: { timeRange: 'week' | 'month' }) {
  return request({
    url: '/api/dashboard/trend',
    method: 'get',
    params
  })
}

// 获取任务优先级分布
export function getTaskPriorityDistribution() {
  return request({
    url: '/api/dashboard/priority-distribution',
    method: 'get'
  })
}

// 获取任务列表
export interface TaskListParams {
  page: number
  pageSize: number
  status?: string
  priority?: string
  keyword?: string
}

export function getTaskList(params: TaskListParams) {
  return request({
    url: '/api/dashboard/tasks',
    method: 'get',
    params
  })
} 