<template>
  <div class="workplace">
    <div class="page-header">
      <h2 class="page-title">工作台</h2>
    </div>

    <!-- 搜索过滤区域 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="任务编号">
          <el-input v-model="filterForm.number" placeholder="请输入任务编号" />
        </el-form-item>
        
        <el-form-item label="任务名称">
          <el-input v-model="filterForm.keyword" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="创建日期">
          <el-date-picker
            v-model="filterForm.createDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="截止日期">
          <el-date-picker
            v-model="filterForm.dueDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="优先级">
          <el-select v-model="filterForm.priority" placeholder="全部">
            <el-option label="全部" value="" />
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>

        <el-form-item label="成员">
          <el-select v-model="filterForm.members" placeholder="全部" multiple>
            <el-option
              v-for="member in memberOptions"
              :key="member.id"
              :label="member.name"
              :value="member.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="filterForm.tags" placeholder="全部" multiple>
            <el-option
              v-for="tag in tagOptions"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 甘特图工具栏 -->
    <div class="gantt-toolbar">
      <div class="left">
        <el-button-group>
          <el-button @click="handlePrevWeek">上一周</el-button>
          <el-button @click="handleNextWeek">下一周</el-button>
        </el-button-group>
      </div>
      <div class="right">
        <el-button type="primary" @click="handleCreateTask">新建任务</el-button>
      </div>
    </div>

    <!-- 甘特图区域 -->
    <el-card class="gantt-card">
      <div class="gantt-container">
        <div class="gantt-header">
          <div class="task-info">
            <div class="task-name">任务名称</div>
            <div class="task-tags">标签</div>
            <div class="task-priority">优先级</div>
          </div>
          <div class="time-scale">
            <div v-for="date in dateList" :key="date" class="date-cell">
              {{ formatDate(date) }}
            </div>
          </div>
        </div>
        
        <div class="gantt-body">
          <div v-for="task in taskList" :key="task.id" class="task-row">
            <div class="task-info">
              <div class="task-name">{{ task.name }}</div>
              <div class="task-tags">
                <el-tag
                  v-for="tag in task.tags"
                  :key="tag"
                  size="small"
                  :type="getTagType(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="task-priority">
                <el-tag :type="getPriorityType(task.priority)" size="small">
                  {{ task.priority }}
                </el-tag>
              </div>
            </div>
            <div class="time-blocks">
              <div
                class="task-block"
                :style="getTaskBlockStyle(task)"
                :class="task.status"
              >
                {{ task.name }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 新建/编辑任务对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="taskDialogType === 'create' ? '新建任务' : '编辑任务'"
      width="600px"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="80px"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="taskForm.startDate"
            type="date"
            placeholder="请选择开始日期"
          />
        </el-form-item>
        
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="taskForm.endDate"
            type="date"
            placeholder="请选择结束日期"
          />
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="标签" prop="tags">
          <el-select v-model="taskForm.tags" multiple placeholder="请选择标签">
            <el-option
              v-for="tag in tagOptions"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="负责人" prop="members">
          <el-select v-model="taskForm.members" multiple placeholder="请选择负责人">
            <el-option
              v-for="member in memberOptions"
              :key="member.id"
              :label="member.name"
              :value="member.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

interface FilterForm {
  number: string;
  keyword: string;
  tags: number[];
  priority: string;
  status: string;
  members: number[];
  createDateRange: [string, string] | [];
  dueDateRange: [string, string] | [];
}

const filterForm = reactive<FilterForm>({
  number: '',
  keyword: '',
  tags: [],
  priority: '',
  status: '',
  members: [],
  createDateRange: [],
  dueDateRange: []
})

// 成员选项
const memberOptions = ref([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' }
])

// 标签选项
const tagOptions = ref([
  { id: 1, name: 'Bug', type: 'danger' },
  { id: 2, name: '功能', type: 'primary' },
  { id: 3, name: '优化', type: 'warning' }
])

// 任务列表数据
const taskList = ref([
  {
    id: 1,
    name: 'Task1',
    tags: ['Bug'],
    priority: 'medium',
    startDate: '2024-11-10',
    endDate: '2024-11-13',
    status: 'in_progress'
  },
  {
    id: 2,
    name: 'Task2',
    tags: ['Bug'],
    priority: 'medium',
    startDate: '2024-11-13',
    endDate: '2024-11-15',
    status: 'pending'
  },
  {
    id: 3,
    name: 'Task3',
    tags: ['Bug'],
    priority: 'medium',
    startDate: '2024-11-12',
    endDate: '2024-11-16',
    status: 'completed'
  },
  {
    id: 4,
    name: 'Task4',
    tags: ['Bug'],
    priority: 'medium',
    startDate: '2024-11-11',
    endDate: '2024-11-14',
    status: 'in_progress'
  }
])

// 日期列表
const dateList = ref<string[]>([])

// 初始化日期列表
const initDateList = () => {
  const today = dayjs()
  const dates: string[] = []
  for (let i = -3; i < 4; i++) {
    dates.push(today.add(i, 'day').format('YYYY-MM-DD'))
  }
  dateList.value = dates
}

// 格式化日期显示
const formatDate = (date: string) => {
  return dayjs(date).format('MM-DD')
}

// 获取任务块样式
const getTaskBlockStyle = (task: any) => {
  const startIndex = dateList.value.indexOf(task.startDate)
  const endIndex = dateList.value.indexOf(task.endDate)
  const width = (endIndex - startIndex + 1) * 100
  
  return {
    left: `${startIndex * 100}px`,
    width: `${width}px`
  }
}

// 获取标签类型
const getTagType = (tag: string): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const tagOption = tagOptions.value.find(t => t.name === tag)
  return tagOption?.type as 'success' | 'warning' | 'info' | 'primary' | 'danger'
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const types = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  } as const
  return types[priority as keyof typeof types] as 'success' | 'warning' | 'info' | 'primary' | 'danger'
}

// 任务对话框相关
const taskDialogVisible = ref(false)
const taskDialogType = ref<'create' | 'edit'>('create')
const taskFormRef = ref<FormInstance>()

interface TaskForm {
  name: string;
  startDate: string;
  endDate: string;
  priority: string;
  tags: number[];
  members: number[];
  description: string;
}

const taskForm = reactive<TaskForm>({
  name: '',
  startDate: '',
  endDate: '',
  priority: '',
  tags: [],
  members: [],
  description: ''
})

const taskRules: FormRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

// 处理搜索
const handleSearch = () => {
  console.log('搜索条件：', filterForm)
  // TODO: 调用接口进行搜索
}

// 处理重置
const handleReset = () => {
  (Object.keys(filterForm) as Array<keyof FilterForm>).forEach((key) => {
    if (key === 'number' || key === 'keyword' || key === 'priority' || key === 'status') {
      filterForm[key] = '';
    } else {
      filterForm[key] = [];
    }
  });
}

// 处理上一周
const handlePrevWeek = () => {
  dateList.value = dateList.value.map(date => 
    dayjs(date).subtract(7, 'day').format('YYYY-MM-DD')
  )
}

// 处理下一周
const handleNextWeek = () => {
  dateList.value = dateList.value.map(date => 
    dayjs(date).add(7, 'day').format('YYYY-MM-DD')
  )
}

// 处理创建任务
const handleCreateTask = () => {
  taskDialogType.value = 'create'
  taskDialogVisible.value = true;
  (Object.keys(taskForm) as Array<keyof TaskForm>).forEach((key) => {
    if (key === 'name' || key === 'startDate' || key === 'endDate' || key === 'priority' || key === 'description') {
      taskForm[key] = '';
    } else {
      taskForm[key] = [];
    }
  })
}

// 处理保存任务
const handleSaveTask = async () => {
  if (!taskFormRef.value) return
  
  try {
    await taskFormRef.value.validate()
    // TODO: 调用接口保存任务
    ElMessage.success('保存成功')
    taskDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败：', error)
  }
}

onMounted(() => {
  initDateList()
})
</script>

<style scoped>
.workplace {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.gantt-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.gantt-container {
  overflow-x: auto;
}

.gantt-header {
  display: flex;
  border-bottom: 1px solid var(--el-border-color);
}

.task-info {
  display: flex;
  min-width: 300px;
  border-right: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
}

.task-name {
  flex: 1;
  padding: 8px;
}

.task-tags {
  width: 100px;
  padding: 8px;
}

.task-priority {
  width: 80px;
  padding: 8px;
}

.time-scale {
  display: flex;
}

.date-cell {
  width: 100px;
  padding: 8px;
  text-align: center;
  border-right: 1px solid var(--el-border-color);
}

.gantt-body {
  position: relative;
}

.task-row {
  display: flex;
  border-bottom: 1px solid var(--el-border-color);
}

.time-blocks {
  position: relative;
  height: 40px;
  flex: 1;
}

.task-block {
  position: absolute;
  height: 30px;
  top: 5px;
  border-radius: 4px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.task-block:hover {
  opacity: 0.8;
}

.task-block.pending {
  background-color: var(--el-color-warning);
}

.task-block.in_progress {
  background-color: var(--el-color-primary);
}

.task-block.completed {
  background-color: var(--el-color-success);
}
</style>
