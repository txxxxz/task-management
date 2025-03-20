<template>
  <div class="task-list-container">
    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" class="filter-form">
        <!-- 第一行：任务编号、任务名称 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Number">
              <el-input v-model="filterForm.number" placeholder="Please enter task number" clearable />
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Name">
              <el-input v-model="filterForm.name" placeholder="Please enter task name" clearable />
            </el-form-item>
          </div>
        </div>
        
        <!-- 第二行：状态、优先级 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Status">
              <el-select v-model="filterForm.status" placeholder="Please select status" clearable style="width: 100%">
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Priority">
              <el-select v-model="filterForm.priority" placeholder="Please select priority" clearable style="width: 100%">
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
        
        <!-- 第三行：成员、标签 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Members">
              <el-select v-model="filterForm.members" placeholder="Please select members" clearable multiple style="width: 100%">
                <el-option
                  v-for="item in memberOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Tags">
              <el-select v-model="filterForm.tags" placeholder="Please select tags" clearable multiple style="width: 100%">
                <el-option
                  v-for="item in tagOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>
        
        <!-- 第四行：开始时间、截止时间 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Start">
              <el-date-picker
                v-model="filterForm.createDateRange"
                type="daterange"
                range-separator="to"
                start-placeholder="Start date"
                end-placeholder="End date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Due">
              <el-date-picker
                v-model="filterForm.dueDateRange"
                type="daterange"
                range-separator="to"
                start-placeholder="Start date"
                end-placeholder="End date"
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

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <div class="left-buttons">
        <el-button type="primary" @click="handleNewTask">
          <el-icon><Plus /></el-icon> New Task
        </el-button>
        <el-button @click="handleImportBatch">Import Batch</el-button>
      </div>
      <div class="right-buttons">
        <el-button @click="handleDownload">
          <el-icon><Download /></el-icon> Download
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-table
      :data="taskList"
      style="width: 100%"
      border
      stripe
      v-loading="loading"
    >
      <template #empty>
        <div style="padding: 30px; text-align: center;">
          <el-empty description="No task data" />
          <el-button style="margin-top: 20px;" type="primary" @click="handleNewTask">Create Task</el-button>
        </div>
      </template>
      <el-table-column prop="number" label="Number" width="80" />
      <el-table-column prop="name" label="Task Name" min-width="200">
        <template #default="{ row }">
          <el-link 
            type="primary" 
            :underline="false"
            @click="handleViewDetail(row)"
          >
            {{ row.name || row.title || 'Unnamed Task' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="Priority" width="100">
        <template #default="{ row }">
          <el-tag
            :type="getPriorityType(row.priority)"
            effect="light"
            size="small"
          >
            {{ getPriorityText(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            effect="light"
            size="small"
          >
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="Deadline" width="160">
        <template #default="{ row }">
          {{ formatDate(row.deadline || row.dueTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Action" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group>
            <el-tooltip content="View Details" placement="top">
              <el-button
                type="primary"
                link
                @click="handleViewDetail(row)"
              >
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="Edit Task" placement="top" v-if="isLeader">
              <el-button
                type="primary"
                link
                @click="handleEditTask(row)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="Delete Task" placement="top" v-if="isLeader">
              <el-button
                type="danger"
                link
                @click="handleDeleteTask(row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-section">
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
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { Search, Refresh, Plus, Download, View, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTaskList, deleteTask } from '@/api/task'
import { formatDate as formatDateUtil } from '@/utils/format'
import type { TaskDetail, TaskQueryParams } from '@/types/task'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断当前用户是否为项目负责人
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

// 定义filterForm的类型，确保status和priority能够接受数字类型
interface FilterForm {
  number: string;
  name: string;
  priority: number | '';
  status: number | '';
  createDateRange: string[];
  dueDateRange: string[];
  members: string[];
  tags: string[];
}

// 筛选表单数据
const filterForm = reactive<FilterForm>({
  number: '',
  name: '',
  priority: '',
  status: '',
  createDateRange: [],
  dueDateRange: [],
  members: [],
  tags: []
})

// 选项数据
const priorityOptions = [
  { label: 'Urgent', value: 4 },
  { label: 'High', value: 3 },
  { label: 'Medium', value: 2 },
  { label: 'Low', value: 1 }
]

const statusOptions = [
  { label: 'Pending', value: 0 },
  { label: 'In Progress', value: 1 },
  { label: 'Completed', value: 2 },
  { label: 'Cancelled', value: 3 }
]

// 从API获取这些选项，这里先使用静态数据
const memberOptions = [
  { label: 'Member 1', value: 'member1' },
  { label: 'Member 2', value: 'member2' },
  { label: 'Member 3', value: 'member3' }
]

const tagOptions = [
  { label: 'Bug', value: 'bug' },
  { label: 'Feature', value: 'feature' },
  { label: 'Optimization', value: 'optimization' }
]

// 表格数据
const loading = ref(false)
const taskList = ref<TaskDetail[]>([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 监听路由参数变化并自动筛选
onMounted(() => {
  // 从URL参数中获取筛选条件
  const { status, priority, keyword } = route.query
  
  if (status) {
    filterForm.status = Number(status)
  }
  
  if (priority) {
    filterForm.priority = Number(priority)
  }
  
  if (keyword) {
    filterForm.name = keyword as string
  }
  
  // 加载任务列表
  fetchTaskList()
})

// 监听筛选条件变化，更新 URL 参数
watch(() => filterForm.status, (newStatus) => {
  updateUrlParams({ status: newStatus })
})

watch(() => filterForm.priority, (newPriority) => {
  updateUrlParams({ priority: newPriority })
})

// 更新 URL 参数
const updateUrlParams = (params: Record<string, any>) => {
  const query = { ...route.query }
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      query[key] = value.toString()
    } else {
      delete query[key]
    }
  })
  router.push({ query })
}

// 从后端获取任务列表
const fetchTaskList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: TaskQueryParams = {
      keyword: filterForm.name,
      status: typeof filterForm.status === 'number' ? filterForm.status : undefined,
      priority: typeof filterForm.priority === 'number' ? filterForm.priority : undefined,
      projectId: route.query.projectId as string,
      startTime: filterForm.createDateRange[0] || undefined,
      endTime: filterForm.createDateRange[1] || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    console.log('请求参数:', params)
    
    const response = await getTaskList(params)
    console.log('API响应:', response)
    
    const { data } = response
    
    // 处理响应数据
    if (data) {
      console.log('响应数据:', data)
      // 检查数据结构
      if (data.items) {
        taskList.value = data.items || []
        total.value = data.total || 0
      } else if (Array.isArray(data)) {
        // 如果直接返回数组
        taskList.value = data
        total.value = data.length
      } else {
        console.error('意外的响应数据格式:', data)
        taskList.value = []
        total.value = 0
      }
    } else {
      taskList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取任务列表失败', error)
    ElMessage.error('获取任务列表失败，请稍后重试')
    // 清空数据
    taskList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 工具方法
const getPriorityType = (priority: number): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  const types: Record<number, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
    4: 'danger',    // 紧急
    3: 'warning',   // 高
    2: 'success',   // 中
    1: 'info'       // 低
  }
  return types[priority] || 'primary'
}

const getPriorityText = (priority: number): string => {
  switch (priority) {
    case 4: return 'Urgent'
    case 3: return 'High'
    case 2: return 'Medium'
    case 1: return 'Low'
    default: return 'Unknown'
  }
}

const getStatusType = (status: number): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  const types: Record<number, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
    0: 'info',       // 待处理
    1: 'warning',    // 进行中
    2: 'success',    // 已完成
    3: 'danger'      // 已取消
  }
  return types[status] || 'primary'
}

const getStatusText = (status: number): string => {
  switch (status) {
    case 0: return 'Pending'
    case 1: return 'In Progress'
    case 2: return 'Completed'
    case 3: return 'Cancelled'
    default: return 'Unknown'
  }
}

const formatDate = (date: string | undefined): string => {
  if (!date) return '未设置'
  return formatDateUtil(date)
}

// 事件处理方法
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchTaskList()
}

const handleReset = () => {
  // 重置筛选表单
  filterForm.number = ''
  filterForm.name = ''
  filterForm.priority = ''
  filterForm.status = ''
  filterForm.createDateRange = []
  filterForm.dueDateRange = []
  filterForm.members = []
  filterForm.tags = []
  
  // 清除 URL 参数
  router.push({ query: {} })
  
  // 重新加载数据
  currentPage.value = 1
  fetchTaskList()
}

const handleViewDetail = (row: TaskDetail) => {
  router.push(`/detail/${row.id}`)
}

const handleNewTask = () => {
  router.push('/create')
}

const handleImportBatch = () => {
  ElMessage.info('批量导入功能即将上线')
}

const handleDownload = () => {
  ElMessage.info('下载功能即将上线')
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTaskList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTaskList()
}

const handleEditTask = (row: TaskDetail) => {
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人才能编辑任务')
    return
  }
  
  // 跳转到编辑页面
  router.push(`/form/${row.id}`)
}

const handleDeleteTask = (row: TaskDetail) => {
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人才能删除任务')
    return
  }

  ElMessageBox.confirm(
    `确定要删除任务"${row.name || row.title || '未命名任务'}"吗？此操作不可恢复！`,
    '删除确认',
    {
      confirmButtonText: 'Confirm Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  )
    .then(async () => {
      try {
        await deleteTask(row.id)
        ElMessage.success('Delete Success')
        // 刷新任务列表
        fetchTaskList()
      } catch (error) {
        console.error('删除任务失败:', error)
        ElMessage.error('Delete Task Failed, Please try again later')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}
</script>

<style scoped>
/* 整体容器 */
.task-list-container {
  padding: 20px;
  background-color: #f5f7fa;
}

/* 搜索筛选区域 */
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
  /* 可见中线辅助线，调试完成后可以注释掉 */
  /* background-color: rgba(0, 0, 255, 0.1); */
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

/* 确保右列的表单项左对齐到中线 */
.filter-item-half:last-child :deep(.el-form-item) {
  margin-left: 10px;
  position: relative;
  left: -20px; /* 微调右列位置，使其更精确地对齐中线 */
}

.filter-item-small {
  flex: 1 1 100px;
  min-width: 100px;
  max-width: 200px;
}

.filter-item-large {
  flex: 3 1 300px;
  min-width: 300px;
}

.filter-item-medium {
  flex: 2 1 200px;
  min-width: 200px;
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

/* 操作按钮区域 */
.operation-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.left-buttons, .right-buttons {
  display: flex;
  gap: 10px;
}

/* 表格区域 */
:deep(.el-table) {
  margin-top: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-table th) {
  background-color: #f2f6fc;
  color: #606266;
  font-weight: 600;
  padding: 12px 0;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-button--link) {
  padding: 4px 8px;
  margin: 0 2px;
}

:deep(.el-button-group) {
  display: flex;
  justify-content: center;
}

/* 分页 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 0;
  width: 100%;
  max-width: 480px;
  display: flex;
  align-items: center;
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

:deep(.el-date-editor--daterange) {
  width: 100% !important;
}

:deep(.el-date-editor) {
  width: 100% !important;
}

:deep(.el-select) {
  width: 100% !important;
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

/* 标签样式 */
:deep(.el-tag) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  height: 24px;
}

/* 按钮样式 */
:deep(.operation-section .el-button) {
  padding: 8px 16px;
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
</style> 