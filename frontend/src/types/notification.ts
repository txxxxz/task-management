/**
 * 通知类型
 */
export interface Notification {
  /**
   * 通知ID
   */
  id: number
  
  /**
   * 通知类型：task_update-任务更新, comment_mention-评论@
   */
  type: 'task_update' | 'comment_mention'
  
  /**
   * 通知内容
   */
  content: string
  
  /**
   * 接收通知的用户ID
   */
  userId: number
  
  /**
   * 是否已读：0-未读，1-已读
   */
  isRead: number
  
  /**
   * 关联ID（如任务ID或评论ID）
   */
  relatedId: number
  
  /**
   * 创建时间
   */
  createTime: string
}

// 添加一个默认导出
export default {
  Notification
}