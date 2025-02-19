import request from '@/utils/request'
import type { TaskComment, CommentParams } from '@/types/task'

// 添加评论
export function addComment(data: CommentParams) {
  return request<TaskComment>({
    url: '/api/comments',
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(id: number) {
  return request<void>({
    url: '/api/comments/' + id,
    method: 'delete'
  })
}

// 获取评论列表
export function getCommentList(taskId: string) {
  return request<TaskComment[]>({
    url: '/api/comments',
    method: 'get',
    params: { taskId }
  })
}