<template>
  <div class="task-list-container">
    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" class="filter-form">
        <!-- 第一行：任务编号、任务名称、优先级、状态 -->
        <div class="filter-row">
          <div class="filter-item">
            <el-form-item label="Task Number">
              <el-input v-model="filterForm.number" placeholder="Please enter the task number" clearable />
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Task Name">
              <el-input v-model="filterForm.name" placeholder="Please enter the task name" clearable />
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Priority">
              <el-select v-model="filterForm.priority" placeholder="Please select the priority" clearable>
                <el-option
                  v-for="item in priorityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Status">
              <el-select v-model="filterForm.status" placeholder="Please select the status" clearable>
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>
        <!-- 第二行：创建日期、截止日期、成员、标签 -->
        <div class="filter-row">
          <div class="filter-item">
            <el-form-item label="Create Date">
              <el-date-picker
                v-model="filterForm.createDateRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Start Date"
                end-placeholder="End Date"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Due Date">
              <el-date-picker
                v-model="filterForm.dueDateRange"
                type="daterange"
                range-separator="To"
                start-placeholder="Start Date"
                end-placeholder="End Date"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Members">
              <el-select v-model="filterForm.members" placeholder="Please select the members" clearable multiple>
                <el-option
                  v-for="item in memberOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item">
            <el-form-item label="Tags">
              <el-select v-model="filterForm.tags" placeholder="Please select the tags" clearable multiple>
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
        <!-- 查询和重置按钮 -->
        <div class="filter-buttons">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> Search
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> Reset
          </el-button>
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
      <el-table-column prop="number" label="Task Number" width="120" />
      <el-table-column prop="name" label="Task Name" min-width="200">
        <template #default="{ row }">
          <el-link 
            type="primary" 
            :underline="false"
            @click="handleViewDetail(row)"
          >
            {{ row.name || row.title }}
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
      <el-table-column prop="status" label="Status" width="120">
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
      <el-table-column prop="createTime" label="Create Date" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="Due Date" width="180">
        <template #default="{ row }">
          {{ formatDate(row.deadline || row.dueTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Operation" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="handleViewDetail(row)"
          >
            Check
          </el-button>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getTaskList } from '@/api/task'
import { formatDate as formatDateUtil } from '@/utils/format'
import type { TaskDetail, TaskQueryParams } from '@/types/task'

const route = useRoute()
const router = useRouter()

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
  { label: '紧急', value: 4 },
  { label: '高', value: 3 },
  { label: '中', value: 2 },
  { label: '低', value: 1 }
]

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

// 从API获取这些选项，这里先使用静态数据
const memberOptions = [
  { label: '成员1', value: 'member1' },
  { label: '成员2', value: 'member2' },
  { label: '成员3', value: 'member3' }
]

const tagOptions = [
  { label: 'Bug', value: 'bug' },
  { label: '功能', value: 'feature' },
  { label: '优化', value: 'optimization' }
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
    
    const response = await getTaskList(params)
    const { data } = response
    
    // 处理响应数据
    if (data) {
      taskList.value = data.items || []
      total.value = data.total || 0
    }
  } catch (error) {
    console.error('获取任务列表失败', error)
    ElMessage.error('获取任务列表失败，请稍后重试')
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
  const textMap: Record<number, string> = {
    4: '紧急',
    3: '高',
    2: '中',
    1: '低'
  }
  return textMap[priority] || '未知'
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
  const textMap: Record<number, string> = {
    0: '待处理',
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return textMap[status] || '未知'
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
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-item {
  flex: 1 1 200px;
  min-width: 280px;
}

.filter-buttons {
  text-align: right;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 0;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-regular);
  padding-right: 12px;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-date-editor.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.el-select),
:deep(.el-date-editor) {
  width: 100%;
}

/* 按钮样式 */
.filter-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.filter-buttons .el-button {
  padding: 8px 24px;
  font-weight: 500;
}

/* 操作按钮区域 */
.operation-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
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
}

:deep(.el-table th) {
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  font-weight: 600;
}

/* 分页 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  margin-top: 20px;
}
</style> 