/**
 * 标签类型定义
 */
export interface Tag {
  id: string;
  name: string;
  color: string;
  projectId?: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 标签列表响应类型
 */
export interface TagListResponse {
  total: number;
  items: Tag[];
} 