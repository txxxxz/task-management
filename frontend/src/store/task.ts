import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Task, Comment, Attachment } from '@/types/models'
import * as taskApi from '@/api/task'
import { ElMessage } from 'element-plus'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const comments = ref<Comment[]>([])
  const attachments = ref<Attachment[]>([])

  const fetchTasks = async (projectId: number) => {
    try {
      const data = await taskApi.getTasks(projectId)
      tasks.value = data
      return true
    } catch (error) {
      return false
    }
  }

  const getTask = async (projectId: number, taskId: number) => {
    try {
      const data = await taskApi.getTaskById(projectId, taskId)
      currentTask.value = data
      await Promise.all([
        fetchComments(taskId),
        fetchAttachments(taskId)
      ])
      return true
    } catch (error) {
      return false
    }
  }

  const createTask = async (projectId: number, data: Partial<Task>) => {
    try {
      await taskApi.createTask(projectId, data)
      await fetchTasks(projectId)
      ElMessage.success('创建成功')
      return true
    } catch (error) {
      return false
    }
  }

  const updateTask = async (projectId: number, taskId: number, data: Partial<Task>) => {
    try {
      await taskApi.updateTask(projectId, taskId, data)
      await fetchTasks(projectId)
      if (currentTask.value?.id === taskId) {
        await getTask(projectId, taskId)
      }
      ElMessage.success('更新成功')
      return true
    } catch (error) {
      return false
    }
  }

  const deleteTask = async (projectId: number, taskId: number) => {
    try {
      await taskApi.deleteTask(projectId, taskId)
      await fetchTasks(projectId)
      if (currentTask.value?.id === taskId) {
        currentTask.value = null
      }
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      return false
    }
  }

  const fetchComments = async (taskId: number) => {
    try {
      const data = await taskApi.getTaskComments(taskId)
      comments.value = data
      return true
    } catch (error) {
      return false
    }
  }

  const createComment = async (taskId: number, content: string) => {
    try {
      await taskApi.createComment(taskId, content)
      await fetchComments(taskId)
      ElMessage.success('评论成功')
      return true
    } catch (error) {
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
      return false
    }
  }

  const fetchAttachments = async (taskId: number) => {
    try {
      const data = await taskApi.getTaskAttachments(taskId)
      attachments.value = data
      return true
    } catch (error) {
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