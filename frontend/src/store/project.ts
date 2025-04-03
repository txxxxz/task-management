import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project } from '@/types/models'
import * as projectApi from '@/api/project'
import { ElMessage } from 'element-plus'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)

  const fetchProjects = async () => {
    try {
      const data = await projectApi.getAllProjects()
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

  const createProject = async (projectData: Partial<Project>) => {
    loading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      const newProject: Project = {
        id: projects.value.length + 1,
        name: projectData.name || '',
        description: projectData.description || '',
        status: projectData.status || 0,
        priority: projectData.priority || 1,
        startTime: projectData.startTime || new Date().toISOString(),
        endTime: projectData.endTime || new Date().toISOString(),
        createTime: new Date().toISOString(),
        createUser: 1,
        updateTime: new Date().toISOString(),
        updateUser: 1
      }
      projects.value.push(newProject)
      ElMessage.success('创建成功')
    } finally {
      loading.value = false
    }
  }

  const updateProject = async (id: number, projectData: Partial<Project>) => {
    loading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value[index] = {
          ...projects.value[index],
          ...projectData,
          updateTime: new Date().toISOString(),
          updateUser: 1
        }
        ElMessage.success('更新成功')
      }
    } finally {
      loading.value = false
    }
  }

  const deleteProject = async (id: number) => {
    loading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value.splice(index, 1)
        ElMessage.success('删除成功')
      }
    } finally {
      loading.value = false
    }
  }

  return {
    projects,
    currentProject,
    loading,
    fetchProjects,
    getProject,
    createProject,
    updateProject,
    deleteProject
  }
}) 