import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project } from '@/types/models'
import * as projectApi from '@/api/project'
import { ElMessage } from 'element-plus'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)

  const fetchProjects = async () => {
    try {
      const data = await projectApi.getProjects()
      projects.value = data
      return true
    } catch (error) {
      return false
    }
  }

  const getProject = async (id: number) => {
    try {
      const data = await projectApi.getProjectById(id)
      currentProject.value = data
      return true
    } catch (error) {
      return false
    }
  }

  const createProject = async (data: Partial<Project>) => {
    try {
      await projectApi.createProject(data)
      await fetchProjects()
      ElMessage.success('创建成功')
      return true
    } catch (error) {
      return false
    }
  }

  const updateProject = async (id: number, data: Partial<Project>) => {
    try {
      await projectApi.updateProject(id, data)
      await fetchProjects()
      if (currentProject.value?.id === id) {
        await getProject(id)
      }
      ElMessage.success('更新成功')
      return true
    } catch (error) {
      return false
    }
  }

  const deleteProject = async (id: number) => {
    try {
      await projectApi.deleteProject(id)
      await fetchProjects()
      if (currentProject.value?.id === id) {
        currentProject.value = null
      }
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      return false
    }
  }

  return {
    projects,
    currentProject,
    fetchProjects,
    getProject,
    createProject,
    updateProject,
    deleteProject
  }
}) 