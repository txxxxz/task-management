<template>
  <div class="kanban-container">
    <!-- 顶部搜索和标签页 -->
    <div class="kanban-header">
      <el-tabs v-model="activeTab" class="kanban-tabs">
        <el-tab-pane label="All" name="all" />
        <el-tab-pane label="Pending" name="pending" />
        <el-tab-pane label="In progress" name="in-process" />
        <el-tab-pane label="Completed" name="completed" />
      </el-tabs>
      <div class="header-right">
        <el-select
          v-model="selectedProject"
          placeholder="Select project"
          clearable
          class="project-select"
          @change="handleProjectChange"
        >
          <el-option
            v-for="project in projectList"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-input
          v-model="searchText"
          placeholder="Search tasks..."
          class="search-input"
          :prefix-icon="Search"
        />
        <el-button v-if="isLeader" type="primary" @click="handleCreateTask">
          <el-icon><Plus /></el-icon> Create task
        </el-button>
      </div>
    </div>

    <!-- 任务看板 -->
    <div class="kanban-board">
      <!-- 待处理任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'pending'" class="task-section">
        <div class="section-header">
          <h2>Pending</h2>
          <span class="task-count">{{ pendingTasks.length }}</span>
        </div>
        <div class="task-row">
          <!-- 新建任务卡片 -->
          <el-card v-if="isLeader" class="task-card add-task-card" @click="handleNavigateToCreate">
            <div class="add-task-content">
              <el-icon :size="32" style="color: #909399;"><Plus /></el-icon>
              <p style="margin-top: 12px; color: #909399;">Click to create a new task</p>
            </div>
          </el-card>
          <!-- 待处理任务列表 -->
          <el-card 
            v-for="task in pendingTasks" 
            :key="task.id" 
            class="task-card" 
            :data-priority="task.priority"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ task.priority }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="card-content">
              <p>创建时间: {{ task.createTime }}</p>
              <p>截止时间: {{ task.dueTime }}</p>
              <div class="tags-container">
                <el-tag v-for="tag in task.tags" :key="tag" size="small" class="mx-1">
                  {{ tag }}
                </el-tag>
              </div>
              <div class="task-members">
                <el-tooltip
                  v-for="member in task.members"
                  :key="member"
                  :content="member"
                  placement="top"
                  effect="light"
                  :show-after="200"
                >
                  <el-avatar
                    :size="28"
                    class="member-avatar"
                    @click.stop="isLeader && handleRemoveMember(task, member)"
                    :title="isLeader ? '点击移除成员' : member"
                  >
                    {{ member.charAt(0) }}
                  </el-avatar>
                </el-tooltip>
                <el-button
                  v-if="isLeader"
                  circle
                  size="small"
                  @click.stop="handleAddMember(task)"
                  class="add-member-btn"
                  :icon="Plus"
                />
              </div>
              <div class="task-actions">
                <el-button type="primary" size="small" @click="handleProcess(task)">
                  Go to process
                </el-button>
                <el-button type="primary" plain size="small" @click="handleComments(task)">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>Comments({{ task.comments }})</span>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 进行中任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'in-process'" class="task-section">
        <div class="section-header">
          <h2>In progress</h2>
          <span class="task-count">{{ inProcessTasks.length }}</span>
        </div>
        <div class="task-row">
          <el-card 
            v-for="task in inProcessTasks" 
            :key="task.id" 
            class="task-card"
            :data-priority="task.priority"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ task.priority }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="task-info">
              <p><span class="label">创建时间:</span> {{ task.createTime }}</p>
              <p><span class="label">截止时间:</span> {{ task.dueTime }}</p>
              <p v-if="isLeader"><span class="label">负责人:</span> {{ task.leader }}</p>
            </div>
            <div class="task-tags">
              <el-tag
                v-for="tag in task.tags"
                :key="tag"
                class="task-tag"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="task-members">
              <el-tooltip
                v-for="member in task.members"
                :key="member"
                :content="member"
                placement="top"
                effect="light"
                :show-after="200"
              >
                <el-avatar
                  :size="28"
                  class="member-avatar"
                  @click.stop="isLeader && handleRemoveMember(task, member)"
                  :title="isLeader ? '点击移除成员' : member"
                >
                  {{ member.charAt(0) }}
                </el-avatar>
              </el-tooltip>
              <el-button
                v-if="isLeader"
                circle
                size="small"
                @click.stop="handleAddMember(task)"
                class="add-member-btn"
                :icon="Plus"
              />
            </div>
            <div class="task-actions">
              <el-button type="primary" size="small" @click="handleFinish(task)">
                Complete
              </el-button>
              <el-button type="primary" plain size="small" @click="handleComments(task)">
                <el-icon><ChatLineRound /></el-icon>
                <span>Comments({{ task.comments }})</span>
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 已完成任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'completed'" class="task-section">
        <div class="section-header">
          <h2>Completed</h2>
          <span class="task-count">{{ completedTasks.length }}</span>
        </div>
        <div class="task-row">
          <el-card 
            v-for="task in completedTasks" 
            :key="task.id" 
            class="task-card"
            :data-priority="task.priority"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ task.priority }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="task-info">
              <p><span class="label">创建时间:</span> {{ task.createTime }}</p>
              <p><span class="label">截止时间:</span> {{ task.dueTime }}</p>
              <p v-if="isLeader"><span class="label">负责人:</span> {{ task.leader }}</p>
            </div>
            <div class="task-tags">
              <el-tag
                v-for="tag in task.tags"
                :key="tag"
                class="task-tag"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="task-members">
              <el-tooltip
                v-for="member in task.members"
                :key="member"
                :content="member"
                placement="top"
                effect="light"
                :show-after="200"
              >
                <el-avatar
                  :size="28"
                  class="member-avatar"
                >
                  {{ member.charAt(0) }}
                </el-avatar>
              </el-tooltip>
            </div>
            <div class="task-actions">
              <el-button type="info" size="small" @click.stop="handleCheck(task)">
                <el-icon><View /></el-icon>
                <span>View</span>
              </el-button>
              <el-button type="primary" plain size="small" @click.stop="handleComments(task)">
                <el-icon><ChatLineRound /></el-icon>
                <span>Comments({{ task.comments }})</span>
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, Plus, Check, ChatLineRound, View, Folder } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { getProjectList } from '../../api/project'
import type { Project } from '../../types/task'
import dayjs from 'dayjs'

const userStore = useUserStore()
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

const router = useRouter()

// 当前激活的标签页
const activeTab = ref<'all' | 'pending' | 'in-process' | 'completed'>('all')
// 搜索文本
const searchText = ref('')

// 项目相关数据
const projectList = ref<Project[]>([])
const selectedProject = ref('')

// 获取项目列表
const fetchProjects = async () => {
  try {
    const { data } = await getProjectList()
    projectList.value = data.data.items
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 处理项目选择变更
const handleProjectChange = (projectId: string) => {
  // 根据选中的项目过滤任务
  if (projectId) {
    tasks.value = tasks.value.filter(task => task.projectId === projectId)
  } else {
    // 重置为所有任务
    initData()
  }
}

// 模拟任务数据
const tasks = ref([
  {
    id: 1,
    title: 'Frontend project refactoring plan',
    createTime: '2024-01-15 09:00:00',
    dueTime: '2024-02-15 18:00:00',
    priority: 'HIGH',
    status: 'pending',
    tags: ['Architecture', 'Refactoring', 'Frontend'],
    members: ['Tom', 'John'],
    comments: 5,
    leader: 'Tom',
    description: 'Refactor the existing frontend project to improve code quality and performance',
    projectId: '1',
    projectName: 'Frontend Refactoring Project'
  }
])

// 初始化数据
const initData = () => {
  tasks.value = [
    {
      id: 1,
      title: 'Frontend project refactoring plan',
      createTime: '2024-01-15 09:00:00',
      dueTime: '2024-02-15 18:00:00',
      priority: 'HIGH',
      status: 'pending',
      tags: ['Architecture', 'Refactoring', 'Frontend'],
      members: ['Tom', 'John'],
      comments: 5,
      leader: 'Tom',
      description: 'Refactor the existing frontend project to improve code quality and performance',
      projectId: '1',
      projectName: 'Frontend Refactoring Project'
    },
    {
      id: 2,
      title: 'Backend interface optimization',
      createTime: '2024-01-16 10:30:00',
      dueTime: '2024-01-30 18:00:00',
      priority: 'MEDIUM',
      status: 'in-process',
      tags: ['Backend', 'Performance optimization'],
      members: ['Amy', 'Jack'],
      comments: 3,
      leader: 'Tom',
      description: 'Optimize the existing backend interfaces to improve response speed',
      projectId: '2',
      projectName: 'Backend Optimization Project'
    },
    {
      id: 3,
      title: 'User feedback system implementation',
      createTime: '2024-01-10 14:00:00',
      dueTime: '2024-01-20 18:00:00',
      priority: 'LOW',
      status: 'completed',
      tags: ['Function', 'User experience'],
      members: ['Sarah', 'Mike'],
      comments: 8,
      leader: 'Tom',
      description: 'Implement the user feedback collection and processing system',
      projectId: '3',
      projectName: 'User Feedback System'
    }
  ]
}

// 在组件挂载时初始化数据
onMounted(() => {
  initData()
  fetchProjects()
})

// 按状态分组的任务列表
const pendingTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'pending')
})

const inProcessTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'in-process')
})

const completedTasks = computed(() => {
  return tasks.value.filter(task => task.status === 'completed')
})

// 获取优先级对应的标签类型
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const types = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'primary',
    'LOW': 'info'
  } as const
  return types[priority as keyof typeof types] || 'info'
}

// 处理任务操作
const handleFinish = (task: any) => {
  task.status = 'completed'
  ElMessage.success('Task completed')
}

const handleCheck = (task: any) => {
  ElMessage('View task details')
}

const handleComments = (task: any) => {
  ElMessage('View task comments')
}

const handleAddMember = (task: any) => {
  ElMessage('Add member feature under development')
}

const handleRemoveMember = (task: any, member: string) => {
  const index = task.members.indexOf(member)
  if (index !== -1) {
    task.members.splice(index, 1)
    ElMessage.success('Member removed')
  }
}

const handleCreateTask = () => {
  ElMessageBox.prompt('Please enter the task title', 'New task', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    inputPattern: /\S+/,
    inputErrorMessage: 'Title cannot be empty'
  }).then(({ value }) => {
    const newTask = {
      id: tasks.value.length + 1,
      title: value,
      createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      dueTime: dayjs().add(7, 'day').format('YYYY-MM-DD HH:mm:ss'),
      priority: 'MEDIUM',
      status: 'pending',
      tags: [],
      members: [],
      comments: 0,
      leader: userStore.userInfo?.username || 'Not assigned',
      description: '',
      projectId: '',
      projectName: ''
    }
    tasks.value.push(newTask)
    ElMessage.success('Task created successfully')
  })
}

const handleNavigateToCreate = () => {
  router.push('/table/create')
}

const handleTaskClick = (task: any) => {
  router.push('/detail/' + task.id)
}

const handleProcess = (task: any) => {
  router.push(`/detail/${task.id}`)
}

const handleComplete = async (task: any) => {
  try {
    await ElMessageBox.confirm('Confirm to mark the task as completed?', 'Tips', {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    task.status = 'completed'
    ElMessage.success('Task completed')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Operation failed')
    }
  }
}
</script>

<style scoped>
.kanban-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.search-input {
  width: 360px;
}

.kanban-board {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.task-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.task-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 16px;
  overflow-x: auto;
}

.task-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 320px;
  width: 320px;
  margin: 10px;
  border: 1px solid #ebeef5;
  border-left: 4px solid transparent;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.task-card-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-project {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.task-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-dates {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.task-members {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-actions {
  display: flex;
  gap: 8px;
}

.priority-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.priority-urgent {
  background-color: #fef0f0;
  color: #f56c6c;
}

.priority-high {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.priority-medium {
  background-color: #ecf5ff;
  color: #409eff;
}

.priority-low {
  background-color: #f0f9eb;
  color: #67c23a;
}

.add-task-card {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 320px;
  width: 320px;
  margin: 10px;
  background: #fafafa;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-task-card:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.add-task-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  padding: 0;
}

.add-task-content .el-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.add-task-content p {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.task-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 20px;
  justify-items: center;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 标签样式 */
.task-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.task-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: #f4f4f5;
  color: #909399;
}

/* 状态标签样式 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background-color: #f4f4f5;
  color: #909399;
}

.status-in-progress {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

/* 头像样式统一 */
.task-members .member-avatar {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #fff;
  background: var(--el-color-primary-light-3);
  color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-right: -12px;
  font-size: 13px;
  font-weight: 500;
}

.task-members .member-avatar:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
  z-index: 2;
  border-color: var(--el-color-primary-light-5);
}

.task-members .member-avatar:not(:first-child) {
  margin-left: 0;
}

.task-actions .el-button {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
}

.task-actions .el-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.task-actions .el-button span {
  visibility: visible !important;
  opacity: 1 !important;
}

.task-actions .el-button:hover {
  opacity: 0.9;
}

.task-actions .el-button:hover span {
  visibility: visible !important;
  opacity: 1 !important;
}

/* 优化分组数量统计样式 */
.task-count {
  margin-left: 12px;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.1);
}

/* 优化卡片内部布局 */
.task-card :deep(.el-card__header) {
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
}

.task-card :deep(.el-card__body) {
  padding: 8px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  font-size: 12px;
  padding: 0;
}

.card-content p {
  margin: 2px 0;
  color: var(--el-text-color-regular);
  line-height: 1.3;
}

.tags-container {
  margin: 4px 0;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.task-members {
  margin: 4px 0;
  padding: 4px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.task-actions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  gap: 8px;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-info {
  display: flex;
  align-items: center;
}

.project-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-lighter);
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
}

.task-info {
  margin: 4px 0;
  line-height: 1.3;
}

.task-info p {
  margin: 2px 0;
  font-size: 12px;
}

.task-info .label {
  font-weight: 500;
  margin-right: 4px;
  color: var(--el-text-color-secondary);
}

.task-tags {
  margin: 8px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.task-tags .el-tag {
  margin: 2px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
}

/* 新增任务卡片样式优化 */
.add-task-card {
  height: 320px;
}

.add-task-content {
  padding: 20px;
  margin-top: 20px;
  margin-bottom: 20px;
  
}

.add-task-content .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  margin-top: 16px;
}

.add-task-content p {
  font-size: 18px;
  margin-top: 16px;
  margin-bottom: 16px;
}

/* 优化按钮样式 */
.task-actions .el-button {
  height: 28px;
  padding: 0 12px;
  font-size: 12px;
}

.task-actions .el-button .el-icon {
  font-size: 14px;
  margin-right: 4px;
}

/* 优化头像样式 */
.task-members .member-avatar {
  width: 24px;
  height: 24px;
  font-size: 12px;
  margin-right: -8px;
}

.add-member-btn {
  width: 24px !important;
  height: 24px !important;
  margin-left: 4px !important;
}

.add-member-btn .el-icon {
  font-size: 12px;
}

/* 优化头部样式 */
.card-header {
  gap: 4px;
}

.header-main {
  margin-bottom: 4px;
}

.project-select {
  width: 200px;
  margin-right: 16px;
}

/* 优化滚动条样式 */
.task-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 4px;
}

.task-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: var(--el-border-color-lighter);
  border-radius: 2px;
}

.task-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: transparent;
}

.task-card[data-priority="CRITICAL"] {
  border-left-color: #F56C6C;
}

.task-card[data-priority="HIGH"] {
  border-left-color: #E6A23C;
}

.task-card[data-priority="MEDIUM"] {
  border-left-color: #409EFF;
}

.task-card[data-priority="LOW"] {
  border-left-color: #67C23A;
}
</style>
