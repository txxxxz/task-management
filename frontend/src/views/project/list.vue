<template>
  <div class="project-list-container">
    <!-- 搜索表单 -->
    <div class="filter-section">
      <el-form :model="searchForm" class="filter-form">
        <!-- 第一行：项目名称、优先级、状态 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Name">
              <el-input 
                v-model="searchForm.name" 
                placeholder="Please enter the project name" 
                clearable 
                prefix-icon="Search"
              />
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Status">
              <el-select v-model="searchForm.status" placeholder="Please select the status" clearable style="width: 100%" >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Priority">
              <el-select v-model="searchForm.priority" placeholder="Please select the priority" clearable style="width: 100%">
                <el-option
                  v-for="item in priorityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>
        
        <!-- 第二行：负责人、成员 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Leader">
              <el-select 
                v-model="searchForm.leader" 
                placeholder="Please select the leader" 
                clearable
                filterable
                remote
                :remote-method="searchProjectLeaders"
                :loading="leadersLoading"
                loading-text="Loading..."
                @visible-change="onLeaderSelectOpen"
                style="width: 100%"
              >
                <el-option
                  v-for="item in leaderOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <div class="member-option">
                    <el-avatar 
                      :size="24" 
                      :src="item.avatar" 
                      class="member-avatar"
                    >
                      {{ item.label.charAt(0).toUpperCase() }}
                    </el-avatar>
                    <span class="member-name">{{ item.label }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Members">
              <el-select 
                v-model="searchForm.members" 
                multiple 
                placeholder="Please select the members" 
                clearable 
                style="width: 100%"
                filterable
                :loading="membersLoading"
                loading-text="Loading..."
                remote
                :remote-method="searchUsers"
                collapse-tags
                collapse-tags-tooltip>
                <template #prefix>
                  <el-icon v-if="membersLoading"><Loading /></el-icon>
                </template>
                <el-option
                  v-for="item in memberOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <div class="member-option">
                    <el-avatar 
                      :size="24" 
                      :src="item.avatar" 
                      class="member-avatar"
                    >
                      {{ item.label.charAt(0).toUpperCase() }}
                    </el-avatar>
                    <span class="member-name">{{ item.label }}</span>
                  </div>
                </el-option>
                <template #empty>
                  <div class="empty-members">
                    <p>Did not find any matching members, please try other keywords</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </div>
        </div>
        
        <!-- 第三行：开始时间、截止时间 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Start">
              <el-date-picker
                v-model="searchForm.startTimeRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Start Date"
                end-placeholder="Due Date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Due">
              <el-date-picker
                v-model="searchForm.endTimeRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Start Date"
                end-placeholder="Due Date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 搜索按钮行 -->
        <div class="filter-buttons-row">
          <div class="filter-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> Search
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon> Reset
            </el-button>
          </div>
        </div>
      </el-form>
    </div>

    <div class="operation-section">
      <div class="left-buttons">
        <el-button type="primary" @click="handleCreate" v-if="isLeader">
          <el-icon><Plus /></el-icon> New Project
        </el-button>
      </div>
    </div>

    <!-- Project List -->
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
        padding: '8px 0'
      }"
      :row-style="{
        height: '46px'
      }"
    >
      <el-table-column type="index" label="No." width="60" align="center" />
      <el-table-column prop="name" label="Project Name" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="Status" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Priority" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" effect="light">
            {{ getPriorityLabel(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Start Time" width="180">
        <template #default="{ row }">
          {{ formatDate(row.startTime) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="Due Time" width="180">
        <template #default="{ row }">
          {{ formatDate(row.endTime) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="Operation" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-buttons">
            <!-- 针对 leader 显示编辑和删除按钮 -->
            <template v-if="isLeader">
              <el-tooltip content="Edit" placement="top">
                <el-button type="primary" link @click="handleProjectAction('edit', row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="Delete" placement="top">
                <el-button type="danger" link @click="handleProjectAction('delete', row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
            <!-- 针对普通成员只显示查看按钮 -->
            <template v-else>
              <el-tooltip content="View" placement="top">
                <el-button type="info" link @click="handleViewDetail(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
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

    <!-- Member Management Dialog -->
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
  Folder,
  Loading,
  View
} from '@element-plus/icons-vue'
import type { Project } from '../../types/task'
import {
  getAllProjects as getProjectList,
  deleteProject,
  updateProject,
  addProjectMember,
  removeProjectMember,
  getProjectMembers
} from '../../api/project'
import { getAllUsers, getAllLeaders, searchLeaders } from '../../api/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
// 成员加载状态
const membersLoading = ref(false)
// 负责人加载状态
const leadersLoading = ref(false)

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
  priority: '',
  leader: '',
  members: [] as string[],
  startTimeRange: [] as string[],
  endTimeRange: [] as string[]
})

// 选项数据
const statusOptions = [
  { label: 'Pending', value: 0 },
  { label: 'In Progress', value: 1 },
  { label: 'Completed', value: 2 },
  { label: 'Archived', value: 3 }
]

// 优先级选项
const priorityOptions = [
  { label: 'Critical', value: 4 },
  { label: 'High', value: 3 },
  { label: 'Medium', value: 2 },
  { label: 'Low', value: 1 }
]

// 修复类型定义
const memberOptions = ref<{ value: string, label: string, avatar?: string }[]>([])
// 负责人选项
const leaderOptions = ref<{ value: string, label: string, avatar?: string }[]>([])

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
    // 名称模糊搜索：如果有搜索关键字，则检查项目名称是否包含该关键字（不区分大小写）
    if (searchForm.name && !project.name.toLowerCase().includes(searchForm.name.toLowerCase())) {
      return false
    }
    
    // 状态筛选：如果选择了状态，则项目状态必须与选择的状态匹配
    if (searchForm.status !== '' && project.status !== Number(searchForm.status)) {
      return false
    }
    
    // 成员模糊搜索：如果选择了成员，则项目成员必须包含至少一个选择的成员
    if (searchForm.members.length > 0) {
      // 检查项目成员中是否有任何一个成员的名称包含搜索关键字
      const hasMatchingMember = searchForm.members.some(searchMember => 
        project.members.some(projectMember => 
          projectMember.toLowerCase().includes(searchMember.toLowerCase())
        )
      )
      if (!hasMatchingMember) return false
    }
    
    return true
  })
})

// 格式化日期为YYYY-MM-DD格式
const formatDateForApi = (dateStr: string): string => {
  try {
    // 确保日期格式为YYYY-MM-DD
    const date = new Date(dateStr);
    return date.toISOString().split('T')[0];
  } catch (e) {
    return dateStr; // 出错则返回原字符串
  }
}

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      keyword: searchForm.name, // 项目名称关键字，后端已支持模糊搜索
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加状态过滤
    if (searchForm.status !== '') {
      params.status = Number(searchForm.status)
    }
    
    // 添加优先级过滤
    if (searchForm.priority !== '') {
      params.priority = Number(searchForm.priority)
    }
    
    // 添加负责人过滤
    if (searchForm.leader) {
      params.leader = searchForm.leader
    }
    
    // 添加成员过滤
    if (searchForm.members.length > 0) {
      params.members = searchForm.members.join(',')
    }
    
    // 添加开始时间范围
    if (searchForm.startTimeRange && searchForm.startTimeRange.length === 2) {
      params.startTime = formatDateForApi(searchForm.startTimeRange[0])
      params.endTime = formatDateForApi(searchForm.startTimeRange[1])
    }
    
    // 添加截止时间范围
    if (searchForm.endTimeRange && searchForm.endTimeRange.length === 2) {
      params.dueStartTime = formatDateForApi(searchForm.endTimeRange[0])
      params.dueEndTime = formatDateForApi(searchForm.endTimeRange[1])
    }
    
    const response = await getProjectList(params)
    if (response.data && (response.data.code === 1 || response.data.code === 200)) {
      projectList.value = response.data.data.items
      total.value = response.data.data.total
    }
  } catch (error) {
    console.error('Failed to get project list:', error)
    ElMessage.error('Get project list failed')
  } finally {
    loading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  membersLoading.value = true
  try {
    const response = await getAllUsers({
      role: '0,1', // Only query users with roles 0 and 1
      page: 1,
      pageSize: 50
    })
    
    if (response && response.data && response.data.code === 1 && response.data.data) {
      const { items } = response.data.data
      if (Array.isArray(items)) {
        memberOptions.value = items.map(user => ({
          value: user.username,
          label: `${user.username} (${user.role === 0 ? 'member' : 'leader'})`,
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
    ElMessage.error('Failed to get user list')
  } finally {
    membersLoading.value = false
  }
}

// 获取项目负责人列表
const fetchLeaders = async () => {
  leadersLoading.value = true
  try {
    const response = await getAllLeaders({
      page: 1,
      pageSize: 50
    })
    
    if (response && response.data && response.data.code === 1 && response.data.data) {
      const { items } = response.data.data
      if (Array.isArray(items)) {
        leaderOptions.value = items.map(user => ({
          value: user.username,
          label: user.username,
          avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }))
      } else {
        leaderOptions.value = []
      }
    } else {
      leaderOptions.value = []
    }
  } catch (error) {
    leaderOptions.value = []
    ElMessage.error('Get project leader list failed')
  } finally {
    leadersLoading.value = false
  }
}

// 搜索用户
const searchUsers = async (query: string) => {
  if (query) {
    membersLoading.value = true
    try {
      const response = await getAllUsers({
        keyword: query, // Support fuzzy search for usernames
        role: '0,1',
        page: 1,
        pageSize: 20
      })
      
      if (response && response.data && response.data.code === 1 && response.data.data) {
        const { items } = response.data.data
        if (Array.isArray(items)) {
          memberOptions.value = items.map(user => ({
            value: user.username,
            label: `${user.username} (${user.role === 0 ? 'member' : 'leader'})`,
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
      ElMessage.error('Failed to search users')
    } finally {
      membersLoading.value = false
    }
  } else {
    fetchUsers()
  }
}

// 搜索项目负责人
const searchProjectLeaders = async (query: string) => {
  if (query) {
    leadersLoading.value = true
    try {
      const response = await searchLeaders(query, {
        page: 1,
        pageSize: 20
      })
      
      if (response && response.data && response.data.code === 1 && response.data.data) {
        const { items } = response.data.data
        if (Array.isArray(items)) {
          leaderOptions.value = items.map(user => ({
            value: user.username,
            label: user.username,
            avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
          }))
        } else {
          leaderOptions.value = []
        }
      } else {
        leaderOptions.value = []
      }
    } catch (error) {
      leaderOptions.value = []
      ElMessage.error('Search project leader failed')
    } finally {
      leadersLoading.value = false
    }
  } else {
    fetchLeaders()
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
    priority: '',
    leader: '',
    members: [],
    startTimeRange: [],
    endTimeRange: []
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
    ElMessage.warning('Only project leaders can perform this operation')
    return
  }

  try {
    switch (action) {
      case 'edit':
        // 确保id是数字
        const projectId = typeof project.id === 'string' ? parseInt(project.id) : project.id;
        
        // 使用正确的路由路径
        const path = `/project/detail/${projectId}`;
        console.log('使用正确的项目详情路径:', path);
        router.push(path);
        break
      case 'delete':
        const deleteResponse = await deleteProject(project.id.toString())
        if (deleteResponse.data.code === 1 || deleteResponse.data.code === 200) {
          ElMessage.success('Project has been deleted')
          fetchProjects()
        }
        break
      default:
        break
    }
  } catch (error: any) {
    console.error('Operation failed:', error)
    ElMessage.error(error.message || 'Operation failed')
  }
}

// 处理查看详情
const handleViewDetail = (row: Project) => {
  // 设置当前项目
  currentProject.value = row;
  // 确保id是数字
  const projectId = typeof row.id === 'string' ? parseInt(row.id) : row.id;
  
  // 使用正确的路由路径
  const path = `/project/detail/${projectId}`;
  console.log('使用正确的项目详情路径:', path);
  router.push(path);
}

// 处理成员管理
const handleMemberManagement = async (project: Project) => {
  // 设置当前项目
  currentProject.value = project;
  
  if (!isLeader.value) {
    ElMessage.warning('Only project leaders can manage members')
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
  return option ? option.label : 'Unknown'
}

// 获取状态标签类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'     // Preparation
    case 1: return 'success'  // In Progress
    case 2: return 'warning'  // Completed
    case 3: return 'danger'   // Archived
    default: return 'info'
  }
}

// 获取优先级标签
const getPriorityLabel = (priority: number) => {
  const option = priorityOptions.find(opt => opt.value === priority)
  return option ? option.label : 'Unknown'
}

// 获取优先级标签类型
const getPriorityType = (priority: number) => {
  switch (priority) {
    case 4: return 'danger'  // 紧急
    case 3: return 'warning' // 高
    case 2: return 'info'    // 中
    case 1: return 'success' // 低
    default: return 'info'
  }
}

// 处理负责人选择框打开事件
const onLeaderSelectOpen = (opened: boolean) => {
  if (opened) {
    // 如果列表为空或者选项很少，则加载数据
    if (leaderOptions.value.length <= 3) {
      fetchLeaders()
    }
  }
}

// 日期格式化函数 - 将任何日期格式转为YYYY-MM-DD HH:mm:ss
const formatDate = (date: string | null | undefined): string => {
  if (!date) return '';
  try {
    // 使用LocalDateTime格式(YYYY-MM-DD HH:mm:ss)
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
  } catch (e) {
    return '';
  }
};

// 初始化
onMounted(() => {
  fetchProjects()
  fetchUsers()
  fetchLeaders()
})
</script>

<style scoped>
.project-list-container {
  padding: 20px;
  background-color: #f5f7fa;
}

.filter-section {
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  position: relative;
}

.filter-row {
  display: flex;
  width: 100%;
  position: relative;
  margin-bottom: 0;
}

.filter-row::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  background-color: transparent;
}

.filter-item-half {
  width: 50%;
  display: flex;
  box-sizing: border-box;
}

.filter-item-half:first-child {
  padding-right: 40px;
  justify-content: flex-start;
}

.filter-item-half:last-child {
  padding-left: 0;
  justify-content: flex-start;
}

.filter-item-half:last-child :deep(.el-form-item) {
  margin-left: 10px;
  position: relative;
  left: -20px;
}

.filter-buttons-row {
  margin-top: 20px;
}

.filter-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.filter-buttons .el-button {
  padding: 6px 16px;
  font-weight: 500;
  min-width: 70px;
  height: 32px;
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
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-right: 12px;
  width: 75px !important;
  min-width: 75px;
  text-align: right;
  line-height: 32px;
  white-space: nowrap;
  overflow: hidden;
}

:deep(.el-form-item__content) {
  flex: 1;
  width: calc(100% - 75px);
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

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.action-buttons :deep(.el-button--link) {
  font-size: 16px;
  padding: 4px;
  margin: 0 2px;
  height: 24px;
  width: 24px;
}

.action-buttons :deep(.el-button--link:hover) {
  color: var(--el-color-primary-light-3);
  transform: scale(1.1);
  transition: all 0.2s ease;
}

.action-buttons :deep(.el-button--link.is-link.is-danger:hover) {
  color: var(--el-color-danger-light-3);
}

:deep(.el-table) {
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 控件样式 */
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  --el-input-height: 32px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-date-editor.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.el-select .el-input__wrapper) {
  height: 32px;
}

:deep(.el-select__tags) {
  height: 30px;
  overflow: hidden;
  padding-top: 3px;
}

:deep(.el-date-editor--daterange) {
  width: 100% !important;
}

:deep(.el-date-editor) {
  width: 100% !important;
}

:deep(.el-select) {
  width: 100% !important;
}

/* 响应式布局调整 */
@media screen and (max-width: 768px) {
  .filter-item-half {
    flex: 0 0 100%;
    min-width: 100%;
  }
  
  .filter-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-buttons {
    justify-content: center;
    width: 100%;
  }
}

.member-option {
  display: flex;
  align-items: center;
  padding: 5px 0;
  width: 100%;
}

.member-avatar {
  margin-right: 8px;
  background-color: #f0f2f5;
  flex-shrink: 0;
}

.member-name {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-members {
  text-align: center;
  padding: 20px 0;
  color: #909399;
  font-size: 14px;
}

/* 已选择的成员标签内的用户名显示优化 */
:deep(.el-select__tags .el-tag) {
  max-width: calc(100% - 10px);
  display: inline-flex;
  align-items: center;
  margin: 2px 4px;
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  transition: all 0.3s;
}

:deep(.el-select__tags .el-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.el-select .el-tag__content) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
  color: #606266;
}
</style> 