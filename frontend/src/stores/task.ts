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
      ElMessage.error('Failed to get task list')
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
      ElMessage.error('Failed to get task details')
      return false
    }
  }

  const createTask = async (projectId: string, data: Omit<CreateTaskParams, 'projectId'>) => {
    try {
      await taskApi.createProjectTask(projectId, data)
      await fetchTasks(projectId)
      ElMessage.success('Created successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to create task')
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
      ElMessage.success('Updated successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to update task')
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
      ElMessage.success('Deleted successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to delete task')
      return false
    }
  }

  const fetchComments = async (taskId: number) => {
    try {
      const response = await taskApi.getTaskComments(taskId)
      comments.value = response.data
      return true
    } catch (error) {
      ElMessage.error('Failed to get comments')
      return false
    }
  }

  const createComment = async (taskId: number, content: string) => {
    try {
      await taskApi.createComment(taskId, { content })
      await fetchComments(taskId)
      ElMessage.success('Comment posted successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to post comment')
      return false
    }
  }

  const deleteComment = async (taskId: number, commentId: number) => {
    try {
      await taskApi.deleteComment(taskId, commentId)
      await fetchComments(taskId)
      ElMessage.success('Deleted successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to delete comment')
      return false
    }
  }

  const fetchAttachments = async (taskId: number) => {
    try {
      const response = await taskApi.getTaskAttachments(taskId)
      attachments.value = response.data
      return true
    } catch (error) {
      ElMessage.error('Failed to get attachments')
      return false
    }
  }

  const uploadAttachment = async (taskId: number, file: File) => {
    try {
      await taskApi.uploadAttachment(taskId, file)
      await fetchAttachments(taskId)
      ElMessage.success('Uploaded successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to upload attachment')
      return false
    }
  }

  const deleteAttachment = async (taskId: number, attachmentId: number) => {
    try {
      await taskApi.deleteAttachment(taskId, attachmentId)
      await fetchAttachments(taskId)
      ElMessage.success('Deleted successfully')
      return true
    } catch (error) {
      ElMessage.error('Failed to delete attachment')
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