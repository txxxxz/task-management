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
          <el-button type="primary" @click="handleCheck">
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
      :data="tableData"
      style="width: 100%"
      border
      stripe
      v-loading="loading"
    >
      <el-table-column prop="taskNumber" label="Task Number" width="120" />
      <el-table-column prop="taskName" label="Task Name" min-width="200" />
      <el-table-column prop="priority" label="Priority" width="100">
        <template #default="{ row }">
          <el-tag
            :type="getPriorityType(row.priority)"
            effect="light"
            size="small"
          >
            {{ row.priority }}
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
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" label="Create Date" width="180" />
      <el-table-column prop="dueDate" label="Due Date" width="180" />
      <el-table-column label="Operation" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="handleCheck()"
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

const route = useRoute()
const router = useRouter()

// 筛选表单数据
const filterForm = reactive({
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
  { label: 'Critical', value: 'Critical' },
  { label: 'High', value: 'High' },
  { label: 'Medium', value: 'Medium' },
  { label: 'Low', value: 'Low' }
]

const statusOptions = [
  { label: 'Pending', value: 'Pending' },
  { label: 'In progress', value: 'In progress' },
  { label: 'Completed', value: 'Completed' },
  { label: 'Expired', value: 'Expired' }
]

const memberOptions = [
  { label: 'Member 1', value: 1 },
  { label: 'Member 2', value: 2 },
  { label: 'Member 3', value: 3 }
]

const tagOptions = [
  { label: 'Bug', value: 'bug' },
  { label: 'Feature', value: 'feature' },
  { label: 'Optimization', value: 'optimization' }
]

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    taskNumber: '232',
    taskName: 'Fix Login Page Authentication Bug',
    priority: 'Critical',
    status: 'In process',
    createDate: '2021-02-28 10:30',
    dueDate: '2021-02-28 10:30'
  },
  {
    taskNumber: '254',
    taskName: 'Fix Login Page Authentication Bug',
    priority: 'High',
    status: 'Completed',
    createDate: '2021-02-28 10:30',
    dueDate: '2021-02-28 10:30'
  },
  // ... 更多数据
])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(687)

// 监听路由参数变化并自动筛选
onMounted(() => {
  const status = route.query.status as string
  if (status) {
    filterForm.status = status
    handleCheck()
  }
})

// 监听筛选条件变化，更新 URL 参数
watch(() => filterForm.status, (newStatus) => {
  updateUrlParams({ status: newStatus })
})

// 更新 URL 参数
const updateUrlParams = (params: Record<string, any>) => {
  const query = { ...route.query }
  Object.entries(params).forEach(([key, value]) => {
    if (value) {
      query[key] = value
    } else {
      delete query[key]
    }
  })
  router.push({ query })
}

// 工具方法
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
    'Critical': 'danger',
    'High': 'warning',
    'Medium': 'success',
    'Low': 'info'
  }
  return types[priority] || 'primary'
}

const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
    'In process': 'warning',
    'Completed': 'success',
    'Pending': 'info'
  }
  return types[status] || 'primary'
}

// 事件处理方法
const handleCheck = () => {
  loading.value = true
  // 构建查询参数
  const params = {
    number: filterForm.number,
    name: filterForm.name,
    priority: filterForm.priority,
    status: filterForm.status,
    createDateStart: filterForm.createDateRange[0],
    createDateEnd: filterForm.createDateRange[1],
    dueDateStart: filterForm.dueDateRange[0],
    dueDateEnd: filterForm.dueDateRange[1],
    members: filterForm.members,
    tags: filterForm.tags
  }
  
  // 模拟 API 调用
  setTimeout(() => {
    // 根据筛选条件过滤数据
    let filteredData = [...tableData.value]
    if (params.status) {
      filteredData = filteredData.filter(item => item.status === params.status)
    }
    if (params.priority) {
      filteredData = filteredData.filter(item => item.priority === params.priority)
    }
    // ... 其他筛选条件
    
    tableData.value = filteredData
    loading.value = false
    ElMessage.success('Query successful')
  }, 1000)
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    const k = key as keyof typeof filterForm
    if (Array.isArray(filterForm[k])) {
      filterForm[k] = [] as any
    } else {
      filterForm[k] = ''
    }
  })
  // 清除 URL 参数
  router.push({ query: {} })
  // 重新加载数据
  handleCheck()
}

const handleNewTask = () => {
  ElMessage.info('Create new task')
}

const handleImportBatch = () => {
  ElMessage.info('Import batch')
}

const handleDownload = () => {
  ElMessage.info('Download')
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  handleCheck()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  handleCheck()
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