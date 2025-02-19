<template>
  <div class="container">
    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="filterForm.number"
            placeholder="请输入任务编号"
            clearable
            prefix-icon="Search"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="filterForm.name"
            placeholder="请输入任务名称"
            clearable
            prefix-icon="Search"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterForm.priority"
            placeholder="请选择优先级"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in priorityOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="6">
          <el-date-picker
            v-model="filterForm.createDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="创建开始日期"
            end-placeholder="创建结束日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="filterForm.dueDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="截止开始日期"
            end-placeholder="截止结束日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterForm.members"
            placeholder="请选择成员"
            clearable
            multiple
            style="width: 100%"
          >
            <el-option
              v-for="item in memberOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterForm.tags"
            placeholder="请选择标签"
            clearable
            multiple
            style="width: 100%"
          >
            <el-option
              v-for="item in tagOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
      </el-row>

      <el-row style="margin-top: 20px">
        <el-col :span="24" style="text-align: right">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 甘特图区域 -->
    <div class="gantt-container">
      <div class="gantt-header">
        <div class="gantt-controls">
          <el-button-group>
            <el-button @click="previousWeek">
              <el-icon><ArrowLeft /></el-icon> 上一周
            </el-button>
            <el-button @click="nextWeek">
              下一周 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>
      
      <!-- 甘特图挂载容器 -->
      <div ref="ganttContainer" class="gantt-chart"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Search, Refresh, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import Gantt from 'frappe-gantt'
import dayjs from 'dayjs'

// 计算当前一周的开始（周一）和结束（周日）日期
const weekStart = computed(() => {
  return currentDate.value.startOf('week').add(1, 'day')  // 周一
})
const weekEnd = computed(() => {
  return weekStart.value.add(6, 'day')  // 周日
})

// 更新甘特图的时间范围
const updateGanttTimeRange = () => {
  if (ganttChart) {
    ganttChart.change_view_mode('Day', weekStart.value.toDate(), weekEnd.value.toDate())
  }
}

// 重新设置时间范围
const previousWeek = () => {
  currentDate.value = currentDate.value.subtract(7, 'days')
  updateGanttTimeRange()
}

const nextWeek = () => {
  currentDate.value = currentDate.value.add(7, 'days')
  updateGanttTimeRange()
}

const currentDate = ref(dayjs())  // 初始日期：当前周的周一

/* ---------------------------
   筛选表单及选项数据
--------------------------- */
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

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
]

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '进行中', value: 'in-progress' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const memberOptions = [
  { label: '成员1', value: 1 },
  { label: '成员2', value: 2 },
  { label: '成员3', value: 3 }
]

const tagOptions = [
  { label: 'Bug', value: 'bug' },
  { label: '功能', value: 'feature' },
  { label: '优化', value: 'optimization' }
]

/* ---------------------------
   甘特图数据和配置
--------------------------- */
const tasks = ref([
  {
    id: 'Task1',
    name: '前端项目重构计划',
    start: dayjs().toDate(),
    end: dayjs().add(4, 'day').toDate(),
    progress: 20,
    priority: 'high',
    tags: ['架构', '重构']
  },
  {
    id: 'Task2',
    name: '后端接口优化',
    start: dayjs().add(3, 'day').toDate(),
    end: dayjs().add(7, 'day').toDate(),
    progress: 60,
    priority: 'medium',
    tags: ['后端', '性能优化']
  },
  {
    id: 'Task3',
    name: '用户反馈系统实现',
    start: dayjs().add(5, 'day').toDate(),
    end: dayjs().add(8, 'day').toDate(),
    progress: 0,
    priority: 'low',
    tags: ['功能', '用户体验']
  },
  {
    id: 'Task4',
    name: '安全漏洞修复',
    start: dayjs().add(1, 'day').toDate(),
    end: dayjs().add(2, 'day').toDate(),
    progress: 100,
    priority: 'urgent',
    tags: ['安全', '漏洞修复']
  }
])

/* ---------------------------
   事件处理方法
--------------------------- */
const handleSearch = () => {
  console.log('搜索条件：', filterForm)
}

const handleReset = () => {
  filterForm.number = ''
  filterForm.name = ''
  filterForm.priority = ''
  filterForm.status = ''
  filterForm.createDateRange = []
  filterForm.dueDateRange = []
  filterForm.members = []
  filterForm.tags = []
}

/* ---------------------------
   自定义任务详情弹窗样式
--------------------------- */
const customPopupHtml = (task: any) => {
  const priorityMap: Record<string, string> = {
    urgent: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  const priorityText = priorityMap[task.priority] || '未知'
  const progressText = `${task.progress || 0}%`
  const startDate = dayjs(task.start).format('YYYY-MM-DD')
  const endDate = dayjs(task.end).format('YYYY-MM-DD')

  return `
    <div class="popup-container">
      <h4 class="task-title">${task.name}</h4>
      <p><strong>开始日期:</strong> ${startDate}</p>
      <p><strong>结束日期:</strong> ${endDate}</p>
      <p><strong>优先级:</strong> ${priorityText}</p>
      <p><strong>进度:</strong> ${progressText}</p>
    </div>
  `
}

/* ---------------------------
   初始化甘特图
--------------------------- */
const ganttContainer = ref(null)
let ganttChart: any = null

const initGantt = () => {
  if (!ganttContainer.value) return

  const ganttTasks = tasks.value.map(task => ({
    id: task.id,
    name: task.name,
    start: task.start,
    end: task.end,
    progress: task.progress,
    dependencies: '',
    custom_class: `priority-${task.priority}`,
    priority: task.priority
  }))

  ganttChart = new Gantt(ganttContainer.value, ganttTasks, {
    header_height: 60,
    column_width: 120,
    step: 24,
    view_modes: ['Day'],
    bar_height: 40,
    bar_corner_radius: 3,
    arrow_curve: 5,
    padding: 18,
    view_mode: 'Day',
    date_format: 'YYYY-MM-DD',
    custom_popup_html: customPopupHtml,
    language: 'zh',
    start_date: weekStart.value.toDate(),
    end_date: weekEnd.value.toDate(),
    on_click: (task: any) => {
      console.log('任务点击：', task)
    },
    on_date_change: (task: any, start: string, end: string) => {
      console.log('日期变更：', task, start, end)
    },
    on_progress_change: (task: any, progress: number) => {
      console.log('进度变更：', task, progress)
    },
    on_view_change: (mode: string) => {
      console.log('视图变更：', mode)
      setTimeout(() => {
        customizeHeader()
      }, 0)
    }
  })

  setTimeout(() => {
    customizeHeader()
  }, 0)
}

// 自定义更新表头，将每个 header cell 修改为两行显示：星期和日期
const customizeHeader = () => {
  const headerCells = document.querySelectorAll('.gantt .grid-header .grid-header-cell')
  headerCells.forEach((cell: Element) => {
    const dateText = (cell as HTMLElement).innerText.trim()
    const dateObj = dayjs(dateText)
    if (dateObj.isValid()) {
      const weekday = getWeekdayLabel(dateObj)
      const formattedDate = dateObj.format('MM-DD');
      (cell as HTMLElement).innerHTML = `
        <div class="date-header">
          <div class="weekday">${weekday}</div>
          <div class="date">${formattedDate}</div>
        </div>
      `
    }
  })
}

// 获取星期几
const getWeekdayLabel = (dateObj: dayjs.Dayjs) => {
  const weekDayMap: Record<number, string> = {
    0: '周日',
    1: '周一',
    2: '周二',
    3: '周三',
    4: '周四',
    5: '周五',
    6: '周六'
  }
  return weekDayMap[dateObj.day()] || ''
}

onMounted(() => {
  initGantt()
})

watch([currentDate], () => {
  updateGanttTimeRange()
})
</script>

<style>
.container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 筛选区域样式 */
.filter-section {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item {
  flex: 1;
  min-width: 240px;
}

.filter-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.el-button {
  margin-left: 12px;
}

/* 甘特图区域样式 */
.gantt-container {
  margin-top: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.gantt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.gantt-controls {
  display: flex;
  gap: 16px;
}

.gantt-chart {
  height: 400px;
  margin-top: 20px;
}

/* 甘特图自定义样式 */
.gantt .bar-wrapper:hover .bar {
  fill-opacity: 0.8;
}

.gantt .bar.priority-urgent {
  fill: #F56C6C;
}

.gantt .bar.priority-high {
  fill: #E6A23C;
}

.gantt .bar.priority-medium {
  fill: #409EFF;
}

.gantt .bar.priority-low {
  fill: #67C23A;
}

.gantt .date-header {
  text-align: center;
  line-height: 1.4;
}

.gantt .date-header .weekday {
  font-weight: bold;
  color: #606266;
}

.gantt .date-header .date {
  font-size: 12px;
  color: #909399;
}

/* 弹窗样式 */
.popup-container {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-width: 200px;
}

.popup-container .task-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.popup-container p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.popup-container p strong {
  color: #303133;
  margin-right: 8px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .filter-item {
    min-width: 100%;
  }
  
  .gantt-chart {
    height: 300px;
  }
}
</style>
