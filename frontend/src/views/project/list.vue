<template>
  <div class="project-list-container">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="Project Name">
            <el-input v-model="searchForm.name" placeholder="Please enter the project name" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="Status">
            <el-select v-model="searchForm.status" placeholder="Please select the status" clearable style="width: 100%">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="Members">
            <el-select v-model="searchForm.members" multiple placeholder="Please select the members" clearable style="width: 100%"
              filterable
              :loading="membersLoading"
              loading-text="Loading..."
              remote
              :remote-method="searchUsers">
              <el-option
                v-for="item in memberOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24" style="text-align: right">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> Search
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> Reset
          </el-button>
          <el-button type="primary" @click="handleCreate" v-if="isLeader">
            <el-icon><Plus /></el-icon> New Project
          </el-button>
        </el-col>
      </el-row>
    </el-form>

    <!-- 项目列表 -->
    <el-table
      v-loading="loading"
      :data="filteredProjects"
      style="width: 100%; margin-top: 20px"
      border
      :header-cell-style="{
        background: '#f5f7fa',
        color: '#606266',
        height: '50px',
        fontWeight: 600
      }"
      :cell-style="{
        padding: '12px 0'
      }"
    >
      <el-table-column prop="name" label="Project Name" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="Description" show-overflow-tooltip />
      <el-table-column label="Leader" width="120">
        <template #default="{ row }">
          {{ row.creator ? row.creator.username : '' }}
        </template>
      </el-table-column>
      <el-table-column label="Status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Members" width="200">
        <template #default="{ row }">
          <el-tooltip
            :content="row.members.join(', ')"
            placement="top"
            :show-after="200"
          >
            <div class="member-list">
              {{ row.members.join(', ') }}
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Operations" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group class="operation-group">
            <el-tooltip content="Edit" placement="top" v-if="isLeader">
              <el-button type="primary" link @click="handleProjectAction('edit', row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Member Management" placement="top" v-if="isLeader">
              <el-button type="primary" link @click="handleMemberManagement(row)">
                <el-icon><User /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Archive" placement="top" v-if="isLeader && row.status === 'ACTIVE'">
              <el-button type="warning" link @click="handleProjectAction('archive', row)">
                <el-icon><Folder /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top" v-if="isLeader">
              <el-button type="danger" link @click="handleProjectAction('delete', row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 成员管理对话框 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="Member Management"
      width="400px"
    >
      <div class="member-list">
        <div v-for="member in currentProjectMembers" :key="member" class="member-item">
          <el-avatar :size="32">{{ member.charAt(0) }}</el-avatar>
          <span class="member-name">{{ member }}</span>
          <el-button
            type="danger"
            link
            @click="handleRemoveMember(member)"
            v-if="isLeader"
          >
            Remove
          </el-button>
        </div>
      </div>
      <div class="add-member" v-if="isLeader">
        <el-select
          v-model="newMember"
          filterable
          placeholder="Add Member"
          class="member-select"
        >
          <el-option
            v-for="user in availableUsers"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          />
        </el-select>
        <el-button type="primary" @click="handleAddMember">Add</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ProjectStatus } from '../../utils/status'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  User,
  Folder
} from '@element-plus/icons-vue'
import type { Project } from '../../types/task'
import {
  getProjectList,
  deleteProject,
  updateProject,
  addProjectMember,
  removeProjectMember,
  getProjectMembers
} from '../../api/project'
import { getAllUsers } from '../../api/user'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
// 成员加载状态
const membersLoading = ref(false)

// 项目列表数据
const projectList = ref<Project[]>([])
// 当前操作的项目
const currentProject = ref<Project | null>(null)

// 判断是否为项目负责人
const isLeader = computed(() => {
  // 全局管理员权限始终为true
  if (userStore.userInfo?.role === 1) {
    return true;
  }
  // 如果没有选择当前项目，则没有项目领导权限
  if (!currentProject.value) return false;
  
  // 特定项目中，如果是创建者也拥有领导权限
  return userStore.userInfo?.id !== undefined && 
         Number(userStore.userInfo.id) === currentProject.value.createUser;
})

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  members: [] as string[]
})

// 选项数据
const statusOptions = [
  { label: '筹备中', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已归档', value: 3 }
]

// 修复类型定义
const memberOptions = ref<{ value: string, label: string, avatar?: string }[]>([])

// 成员管理相关
const memberDialogVisible = ref(false)
const currentProjectMembers = ref<string[]>([])
const newMember = ref('')
const currentProjectId = ref('')

// 可添加的用户列表
const availableUsers = computed(() => {
  return memberOptions.value.filter(
    user => !currentProjectMembers.value.includes(user.value)
  )
})

// 过滤后的项目列表
const filteredProjects = computed(() => {
  return projectList.value.filter(project => {
    if (searchForm.name && !project.name.toLowerCase().includes(searchForm.name.toLowerCase())) return false
    if (searchForm.status && project.status !== Number(searchForm.status)) return false
    if (searchForm.members.length && !searchForm.members.some(member => project.members.includes(member))) return false
    return true
  })
})

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.name,
      status: searchForm.status ? Number(searchForm.status) : undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    const response = await getProjectList(params)
    if (response.data && (response.data.code === 1 || response.data.code === 200)) {
      projectList.value = response.data.data.items
      total.value = response.data.data.total
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  membersLoading.value = true
  try {
    const response = await getAllUsers({
      role: '0,1', // 只查询角色为0和1的用户
      page: 1,
      pageSize: 50
    })
    
    if (response && response.data && response.data.code === 1 && response.data.data) {
      const { items } = response.data.data
      if (Array.isArray(items)) {
        memberOptions.value = items.map(user => ({
          value: user.username,
          label: `${user.username} (${user.role === 0 ? '成员' : '负责人'})`,
          avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }))
      } else {
        memberOptions.value = []
      }
    } else {
      memberOptions.value = []
    }
  } catch (error) {
    memberOptions.value = []
    ElMessage.error('获取用户列表失败')
  } finally {
    membersLoading.value = false
  }
}

// 搜索用户
const searchUsers = async (query: string) => {
  if (query) {
    membersLoading.value = true
    try {
      const response = await getAllUsers({
        keyword: query,
        role: '0,1',
        page: 1,
        pageSize: 20
      })
      
      if (response && response.data && response.data.code === 1 && response.data.data) {
        const { items } = response.data.data
        if (Array.isArray(items)) {
          memberOptions.value = items.map(user => ({
            value: user.username,
            label: `${user.username} (${user.role === 0 ? '成员' : '负责人'})`,
            avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
          }))
        } else {
          memberOptions.value = []
        }
      } else {
        memberOptions.value = []
      }
    } catch (error) {
      memberOptions.value = []
      ElMessage.error('搜索用户失败')
    } finally {
      membersLoading.value = false
    }
  } else {
    fetchUsers()
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

// 处理重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: '',
    members: []
  })
  handleSearch()
}

// 处理新建项目
const handleCreate = () => {
  router.push('/project/create')
}

// 处理项目操作
const handleProjectAction = async (action: string, project: Project) => {
  // 设置当前项目
  currentProject.value = project;
  
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人才能执行此操作')
    return
  }

  try {
    switch (action) {
      case 'edit':
        router.push(`/project/edit/${project.id}`)
        break
      case 'archive':
        const archiveResponse = await updateProject(project.id, { 
          status: 3 as ProjectStatus 
        })
        if (archiveResponse.data.code === 1 || archiveResponse.data.code === 200) {
          ElMessage.success('项目已归档')
          fetchProjects()
        }
        break
      case 'delete':
        const deleteResponse = await deleteProject(project.id)
        if (deleteResponse.data.code === 1 || deleteResponse.data.code === 200) {
          ElMessage.success('项目已删除')
          fetchProjects()
        }
        break
      default:
        break
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理查看详情
const handleViewDetail = (row: Project) => {
  // 设置当前项目
  currentProject.value = row;
  router.push(`/project/edit/${row.id}`)
}

// 处理成员管理
const handleMemberManagement = async (project: Project) => {
  // 设置当前项目
  currentProject.value = project;
  
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人才能管理成员')
    return
  }
  currentProjectId.value = project.id.toString()
  try {
    const response = await getProjectMembers(project.id)
    if (response.data.code === 200) {
      currentProjectMembers.value = response.data.data
      memberDialogVisible.value = true
    }
  } catch (error) {
    console.error('Failed to get project members:', error)
    ElMessage.error('Failed to get project members')
  }
}

// 处理添加成员
const handleAddMember = async () => {
  if (!newMember.value) return
  
  try {
    const response = await addProjectMember(parseInt(currentProjectId.value), newMember.value)
    if (response.data.code === 200) {
      currentProjectMembers.value.push(newMember.value)
      newMember.value = ''
      ElMessage.success('Add member successfully')
    }
  } catch (error) {
    console.error('Failed to add member:', error)
    ElMessage.error('Failed to add member')
  }
}

// 处理移除成员
const handleRemoveMember = async (member: string) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to remove this member?', 'Tips', {
      type: 'warning'
    })
    const response = await removeProjectMember(parseInt(currentProjectId.value), member)
    if (response.data.code === 200) {
      const index = currentProjectMembers.value.indexOf(member)
      if (index !== -1) {
        currentProjectMembers.value.splice(index, 1)
      }
      ElMessage.success('Remove member successfully')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to remove member:', error)
      ElMessage.error('Failed to remove member')
    }
  }
}

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchProjects()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProjects()
}

// 获取状态标签
const getStatusLabel = (status: number) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option ? option.label : '未知'
}

// 获取状态标签类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'     // 筹备中
    case 1: return 'success'  // 进行中
    case 2: return 'warning'  // 已完成
    case 3: return 'danger'   // 已归档
    default: return 'info'
  }
}

// 初始化
onMounted(() => {
  fetchProjects()
  fetchUsers()
})
</script>

<style scoped>
.project-list-container {
  padding: 20px;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.member-list {
  max-height: 300px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.member-name {
  margin-left: 12px;
  flex: 1;
}

.add-member {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.member-select {
  flex: 1;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-button-group) {
  .el-button--link {
    padding: 4px 8px;
  }
}

.operation-group {
  display: flex;
  justify-content: center;
  gap: 4px;
}

:deep(.el-table) {
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style> 