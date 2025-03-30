import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Task, Comment, Attachment } from '@/types/models'
import * as taskApi from '@/api/task'
import { ElMessage } from 'element-plus'
import type { TaskDetail, CreateTaskParams, UpdateTaskParams } from '@/types/task'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<TaskDetail[]>([])
  const currentTask = ref<TaskDetail | null>(null)
  const comments = ref<Comment[]>([])
  const attachments = ref<Attachment[]>([])

  const fetchTasks = async (projectId: string) => {
    try {
      const response = await taskApi.getProjectTasks(projectId)
      tasks.value = response.data.items
      return true
    } catch (error) {
      ElMessage.error('获取任务列表失败')
      return false
    }
  }

  const getTask = async (projectId: string, taskId: string) => {
    try {
      const response = await taskApi.getTaskDetail(taskId)
      currentTask.value = response.data
      await Promise.all([
        fetchComments(Number(taskId)),
        fetchAttachments(Number(taskId))
      ])
      return true
    } catch (error) {
      ElMessage.error('获取任务详情失败')
      return false
    }
  }

  const createTask = async (projectId: string, data: Omit<CreateTaskParams, 'projectId'>) => {
    try {
      await taskApi.createProjectTask(projectId, data)
      await fetchTasks(projectId)
      ElMessage.success('创建成功')
      return true
    } catch (error) {
      ElMessage.error('创建任务失败')
      return false
    }
  }

  const updateTask = async (projectId: string, taskId: string, data: UpdateTaskParams) => {
    try {
      await taskApi.updateTask(taskId, data)
      await fetchTasks(projectId)
      if (currentTask.value?.id === taskId) {
        await getTask(projectId, taskId)
      }
      ElMessage.success('更新成功')
      return true
    } catch (error) {
      ElMessage.error('更新任务失败')
      return false
    }
  }

  const deleteTask = async (projectId: string, taskId: string) => {
    try {
      await taskApi.deleteTask(taskId)
      await fetchTasks(projectId)
      if (currentTask.value?.id === taskId) {
        currentTask.value = null
      }
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      ElMessage.error('删除任务失败')
      return false
    }
  }

  const fetchComments = async (taskId: number) => {
    try {
      const response = await taskApi.getTaskComments(taskId)
      comments.value = response.data
      return true
    } catch (error) {
      ElMessage.error('获取评论失败')
      return false
    }
  }

  const createComment = async (taskId: number, content: string) => {
    try {
      await taskApi.createComment(taskId, { content })
      await fetchComments(taskId)
      ElMessage.success('评论成功')
      return true
    } catch (error) {
      ElMessage.error('发表评论失败')
      return false
    }
  }

  const deleteComment = async (taskId: number, commentId: number) => {
    try {
      await taskApi.deleteComment(taskId, commentId)
      await fetchComments(taskId)
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      ElMessage.error('删除评论失败')
      return false
    }
  }

  const fetchAttachments = async (taskId: number) => {
    try {
      const response = await taskApi.getTaskAttachments(taskId)
      attachments.value = response.data
      return true
    } catch (error) {
      ElMessage.error('获取附件失败')
      return false
    }
  }

  const uploadAttachment = async (taskId: number, file: File) => {
    try {
      await taskApi.uploadAttachment(taskId, file)
      await fetchAttachments(taskId)
      ElMessage.success('上传成功')
      return true
    } catch (error) {
      ElMessage.error('上传附件失败')
      return false
    }
  }

  const deleteAttachment = async (taskId: number, attachmentId: number) => {
    try {
      await taskApi.deleteAttachment(taskId, attachmentId)
      await fetchAttachments(taskId)
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      ElMessage.error('删除附件失败')
      return false
    }
  }

  return {
    tasks,
    currentTask,
    comments,
    attachments,
    fetchTasks,
    getTask,
    createTask,
    updateTask,
    deleteTask,
    createComment,
    deleteComment,
    uploadAttachment,
    deleteAttachment
  }
}) 