<template>
  <div class="update-task-container">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="任务编号">
            <el-input v-model="searchForm.taskNumber" placeholder="请输入任务编号" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="任务名称">
            <el-input v-model="searchForm.taskName" placeholder="请输入任务名称" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="优先级">
            <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable style="width: 100%">
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
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
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
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.createTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="截止时间">
            <el-date-picker
              v-model="searchForm.dueTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="参与成员">
            <el-select v-model="searchForm.members" multiple placeholder="请选择成员" clearable style="width: 100%">
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
          <el-form-item label="标签">
            <el-select v-model="searchForm.tags" multiple placeholder="请选择标签" clearable style="width: 100%">
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
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="primary" @click="handleCreate" v-if="isLeader">
            <el-icon><Plus /></el-icon> 新建任务
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
      <el-table-column prop="number" label="任务编号" width="120" />
      <el-table-column label="任务名称" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="优先级" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" size="small">
            {{ row.priority === 'CRITICAL' ? '紧急' :
               row.priority === 'HIGH' ? '高' :
               row.priority === 'MEDIUM' ? '中' : '低' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ row.status === 'PENDING' ? '待处理' :
               row.status === 'IN_PROGRESS' ? '进行中' :
               row.status === 'COMPLETED' ? '已完成' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="参与成员" width="160">
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
      <el-table-column label="创建时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="截止时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.dueTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group class="operation-group">
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="完成" placement="top">
              <el-button 
                type="success" 
                link 
                @click="handleComplete(row)"
                :disabled="row.status === 'COMPLETED'"
              >
                <el-icon><CircleCheck /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
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
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  CircleCheck,
  Delete
} from '@element-plus/icons-vue'
import type { TaskDetail } from '@/types/task'
import { getTaskList, deleteTask } from '@/api/task'
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
    name: '前端项目重构',
    description: '对现有前端项目进行重构，提高代码质量和性能',
    createTime: '2024-01-15 09:00:00',
    dueTime: '2024-02-15 18:00:00',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    members: ['Tom', 'John'],
    tags: ['重构', '前端'],
    files: [],
    comments: [],
    projectId: '1',
    projectName: '前端重构项目'
  },
  {
    id: '2',
    number: 'TASK-002',
    name: '后端接口优化',
    description: '优化现有后端接口，提高响应速度',
    createTime: '2024-01-16 10:30:00',
    dueTime: '2024-01-30 18:00:00',
    priority: 'MEDIUM',
    status: 'IN_PROGRESS',
    members: ['Amy', 'Jack'],
    tags: ['后端', '性能优化'],
    files: [],
    comments: [],
    projectId: '2',
    projectName: '后端优化项目'
  },
  {
    id: '3',
    number: 'TASK-003',
    name: '用户反馈系统实现',
    description: '实现用户反馈收集和处理系统',
    createTime: '2024-01-10 14:00:00',
    dueTime: '2024-01-20 18:00:00',
    priority: 'LOW',
    status: 'DONE',
    members: ['Sarah', 'Mike'],
    tags: ['功能', '用户体验'],
    files: [],
    comments: [],
    projectId: '3',
    projectName: '用户反馈系统'
  },
  {
    id: '4',
    number: 'TASK-004',
    name: '安全漏洞修复',
    description: '修复最近发现的安全漏洞',
    createTime: '2024-01-17 11:00:00',
    dueTime: '2024-01-19 18:00:00',
    priority: 'CRITICAL',
    status: 'IN_PROGRESS',
    members: ['Tom', 'Amy'],
    tags: ['安全', 'Bug修复'],
    files: [],
    comments: [],
    projectId: '4',
    projectName: '系统安全项目'
  }
])

// 选项数据
const priorityOptions = [
  { label: '紧急', value: 'CRITICAL' },
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const memberOptions = [
  { label: 'Tom', value: 'Tom' },
  { label: 'John', value: 'John' },
  { label: 'Amy', value: 'Amy' },
  { label: 'Jack', value: 'Jack' }
]

const tagOptions = [
  { label: 'Bug', value: 'Bug' },
  { label: '功能开发', value: '功能开发' },
  { label: '性能优化', value: '性能优化' },
  { label: '文档', value: '文档' }
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
    ElMessage.error(err.message || '获取任务列表失败')
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
    await ElMessageBox.confirm('确认将该任务标记为已完成？', '提示', {
      type: 'warning'
    })
    // TODO: 调用完成任务的API
    ElMessage.success('操作成功')
    fetchTasks()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 处理删除
const handleDelete = async (row: TaskDetail) => {
  try {
    await ElMessageBox.confirm('确认删除该任务？此操作不可恢复', '警告', {
      type: 'warning'
    })
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    fetchTasks()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
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
