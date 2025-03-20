import request from '@/utils/request'
import type { TaskFile } from '@/types/task'

// 上传文件到阿里云OSS
export function uploadFile(data: FormData) {
  return request<TaskFile>({
    url: '/api/files/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 上传任务文件到阿里云OSS的task/文件夹
export function uploadTaskFile(data: FormData) {
  return request<TaskFile>({
    url: '/api/files/task/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 批量上传任务文件到阿里云OSS的task/文件夹
export function batchUploadTaskFiles(files: File[]) {
  const formData = new FormData()
  
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request<{
    code: number
    data: string[]
    message: string
  }>({
    url: '/api/files/task/batch-upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 删除文件
export function deleteFile(id: string) {
  return request<void>({
    url: '/api/files/' + id,
    method: 'delete'
  })
}

// 获取文件列表
export function getFileList(taskId: string) {
  return request<TaskFile[]>({
    url: '/api/files',
    method: 'get',
    params: { taskId }
  })
} 