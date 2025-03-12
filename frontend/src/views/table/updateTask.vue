<template>
  <div class="update-task-container">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="Task Number">
            <el-input v-model="searchForm.taskNumber" placeholder="Please enter the task number" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="Task Name">
            <el-input v-model="searchForm.taskName" placeholder="Please enter the task name" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="6">
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
        </el-col>
        <el-col :span="6">
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
      </el-row>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="Create Time">
            <el-date-picker
              v-model="searchForm.createTimeRange"
              type="daterange"
              range-separator="To"
              start-placeholder="Start Date"
              end-placeholder="End Date"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="Due Time">
            <el-date-picker
              v-model="searchForm.dueTimeRange"
              type="daterange"
              range-separator="To"
              start-placeholder="Start Date"
              end-placeholder="End Date"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="Members">
            <el-select v-model="searchForm.members" multiple placeholder="Please select the members" clearable style="width: 100%">
              <el-option
                v-for="item in memberOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="Tags">
            <el-select v-model="searchForm.tags" multiple placeholder="Please select the tags" clearable style="width: 100%">
              <el-option
                v-for="item in tagOptions"
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
            <el-icon><Plus /></el-icon> Create Task
          </el-button>
        </el-col>
      </el-row>
    </el-form>

    <!-- 任务列表 -->
    <el-table
      v-loading="loading"
      :data="filteredTasks"
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
      <el-table-column prop="number" label="Task Number" width="120" />
      <el-table-column label="Task Name" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="Priority" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" size="small">
            {{ row.priority === 'CRITICAL' ? 'Critical' :
               row.priority === 'HIGH' ? 'High' :
               row.priority === 'MEDIUM' ? 'Medium' : 'Low' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ row.status === 'PENDING' ? 'Pending' :
               row.status === 'IN_PROGRESS' ? 'In Progress' :
               row.status === 'COMPLETED' ? 'Completed' : 'Cancelled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Members" width="160">
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
      <el-table-column label="Create Time" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Due Time" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.dueTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Operation" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group class="operation-group">
            <el-tooltip content="Edit" placement="top">
              <el-button type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Complete" placement="top">
              <el-button 
                type="success" 
                link 
                @click="handleComplete(row)"
                :disabled="row.status === 'COMPLETED'"
              >
                <el-icon><CircleCheck /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="Delete" placement="top">
              <el-button
                type="danger"
                link
                @click="handleDelete(row)"
                v-if="isLeader"
              >
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  CircleCheck,
  Delete
} from '@element-plus/icons-vue'
import type { TaskDetail } from '../../types/task'
import { getTaskList, deleteTask } from '../../api/task'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 判断是否为leader
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

// 搜索表单
const searchForm = reactive({
  taskNumber: '',
  taskName: '',
  priority: '',
  status: '',
  createTimeRange: [] as string[],
  dueTimeRange: [] as string[],
  members: [] as string[],
  tags: [] as string[]
})

// 任务列表数据
const taskList = ref<TaskDetail[]>([
  {
    id: '1',
    number: 'TASK-001',
    name: 'Frontend Project Refactoring',
    description: 'Refactor the existing frontend project to improve code quality and performance',
    createTime: '2024-01-15 09:00:00',
    dueTime: '2024-02-15 18:00:00',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    members: ['Tom', 'John'],
    tags: ['Refactoring', 'Frontend'],
    files: [],
    comments: [],
    projectId: '1',
    projectName: 'Frontend Refactoring Project'
  },
  {
    id: '2',
    number: 'TASK-002',
    name: 'Backend Interface Optimization',
    description: 'Optimize the existing backend interfaces to improve response speed',
    createTime: '2024-01-16 10:30:00',
    dueTime: '2024-01-30 18:00:00',
    priority: 'MEDIUM',
    status: 'IN_PROGRESS',
    members: ['Amy', 'Jack'],
    tags: ['Backend', 'Performance Optimization'],
    files: [],
    comments: [],
    projectId: '2',
    projectName: 'Backend Optimization Project'
  },
  {
    id: '3',
    number: 'TASK-003',
    name: 'User Feedback System Implementation',
    description: 'Implement a user feedback collection and processing system',
    createTime: '2024-01-10 14:00:00',
    dueTime: '2024-01-20 18:00:00',
    priority: 'LOW',
    status: 'DONE',
    members: ['Sarah', 'Mike'],
    tags: ['Function', 'User Experience'],
    files: [],
    comments: [],
    projectId: '3',
    projectName: 'User Feedback System Project'
  },
  {
    id: '4',
    number: 'TASK-004',
    name: 'Security Vulnerability Fix',
    description: 'Fix the recently discovered security vulnerabilities',
    createTime: '2024-01-17 11:00:00',
    dueTime: '2024-01-19 18:00:00',
    priority: 'CRITICAL',
    status: 'IN_PROGRESS',
    members: ['Tom', 'Amy'],
    tags: ['Security', 'Bug Fix'],
    files: [],
    comments: [],
    projectId: '4',
    projectName: 'System Security Project'
  }
])

// 选项数据
const priorityOptions = [
  { label: 'Critical', value: 'CRITICAL' },
  { label: 'High', value: 'HIGH' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'Low', value: 'LOW' }
]

const statusOptions = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' }
]

const memberOptions = [
  { label: 'Tom', value: 'Tom' },
  { label: 'John', value: 'John' },
  { label: 'Amy', value: 'Amy' },
  { label: 'Jack', value: 'Jack' }
]

const tagOptions = [
  { label: 'Bug', value: 'Bug' },
  { label: 'Function Development', value: 'Function Development' },
  { label: 'Performance Optimization', value: 'Performance Optimization' },
  { label: 'Documentation', value: 'Documentation' }
]

// 过滤后的任务列表
const filteredTasks = computed(() => {
  return taskList.value.filter(task => {
    if (searchForm.taskNumber && !task.number.toLowerCase().includes(searchForm.taskNumber.toLowerCase())) return false
    if (searchForm.taskName && !task.name.toLowerCase().includes(searchForm.taskName.toLowerCase())) return false
    if (searchForm.priority && task.priority !== searchForm.priority) return false
    if (searchForm.status && task.status !== searchForm.status) return false
    if (searchForm.members.length && !searchForm.members.some(member => task.members.includes(member))) return false
    if (searchForm.tags.length && !searchForm.tags.some(tag => task.tags.includes(tag))) return false
    return true
  })
})

// 获取任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    // 使用模拟数据
    total.value = taskList.value.length
  } catch (err: any) {
    ElMessage.error(err.message || 'Get task list failed')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTasks()
}

// 处理重置
const handleReset = () => {
  Object.assign(searchForm, {
    taskNumber: '',
    taskName: '',
    priority: '',
    status: '',
    createTimeRange: [],
    dueTimeRange: [],
    members: [],
    tags: []
  })
  handleSearch()
}

// 处理新建任务
const handleCreate = () => {
  router.push('/task/create')
}

// 处理查看详情
const handleViewDetail = (row: TaskDetail) => {
  router.push('/detail/' + row.id)
}

// 处理编辑
const handleEdit = (row: TaskDetail) => {
  router.push('/detail/' + row.id)
}

// 处理完成任务
const handleComplete = async (row: TaskDetail) => {
  try {
    await ElMessageBox.confirm('Are you sure to mark this task as completed?', 'Tips', {
      type: 'warning'
    })
    // TODO: 调用完成任务的API
    ElMessage.success('Operation successful')
    fetchTasks()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('Operation failed')
    }
  }
}

// 处理删除
const handleDelete = async (row: TaskDetail) => {
  try {
    await ElMessageBox.confirm('Are you sure to delete this task? This operation cannot be recovered', 'Warning', {
      type: 'warning'
    })
    await deleteTask(row.id)
    ElMessage.success('Delete successful')
    fetchTasks()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('Delete failed')
    }
  }
}

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTasks()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTasks()
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取优先级标签类型
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'danger' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'info',
    'LOW': 'success'
  }
  return types[priority] || 'info'
}

// 获取状态标签类型
const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

// 初始化
onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.update-task-container {
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
  padding: 0 8px;
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
