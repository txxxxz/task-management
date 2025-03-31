<template>
  <div class="container">

    <!-- 甘特图区域 -->
    <div class="gantt-container">
      <div class="gantt-header">
        <div class="gantt-controls">
          <el-button-group>
            <el-button @click="previousWeek">
              <el-icon><ArrowLeft /></el-icon> Previous Week
            </el-button>
            <el-button @click="nextWeek">
              Next Week <el-icon><ArrowRight /></el-icon>
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
  { label: 'Low', value: 'low' },
  { label: 'Medium', value: 'medium' },
  { label: 'High', value: 'high' },
  { label: 'Critical', value: 'critical' }
]

const statusOptions = [
  { label: 'Pending', value: 'pending' },
  { label: 'In Progress', value: 'in-progress' },
  { label: 'Completed', value: 'completed' },
  { label: 'Cancelled', value: 'cancelled' }
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

/* ---------------------------
   甘特图数据和配置
--------------------------- */
const tasks = ref([
  {
    id: 'Task1',
    name: 'Frontend Project Refactoring Plan',
    start: dayjs().toDate(),
    end: dayjs().add(4, 'day').toDate(),
    progress: 20,
    priority: 'high',
    tags: ['Architecture', 'Refactoring']
  },
  {
    id: 'Task2',
    name: 'Backend Interface Optimization',
    start: dayjs().add(3, 'day').toDate(),
    end: dayjs().add(7, 'day').toDate(),
    progress: 60,
    priority: 'medium',
    tags: ['Backend', 'Performance Optimization']
  },
  {
    id: 'Task3',
    name: 'User Feedback System Implementation',
    start: dayjs().add(5, 'day').toDate(),
    end: dayjs().add(8, 'day').toDate(),
    progress: 0,
    priority: 'low',
    tags: ['Feature', 'User Experience']
  },
  {
    id: 'Task4',
    name: 'Security Vulnerability Fix',
    start: dayjs().add(1, 'day').toDate(),
    end: dayjs().add(2, 'day').toDate(),
    progress: 100,
    priority: 'critical',
    tags: ['Security', 'Vulnerability Fix']
  }
])

/* ---------------------------
   事件处理方法
--------------------------- */
const handleSearch = () => {
  console.log('Search conditions:', filterForm)
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
    urgent: 'Critical',
    high: 'High',
    medium: 'Medium',
    low: 'Low'
  }
  const priorityText = priorityMap[task.priority] || 'Unknown'
  const progressText = `${task.progress || 0}%`
  const startDate = dayjs(task.start).format('YYYY-MM-DD HH:mm:ss')
  const endDate = dayjs(task.end).format('YYYY-MM-DD HH:mm:ss')

  return `
    <div class="popup-container">
      <h4 class="task-title">${task.name}</h4>
      <p><strong>Start Date:</strong> ${startDate}</p>
      <p><strong>End Date:</strong> ${endDate}</p>
      <p><strong>Priority:</strong> ${priorityText}</p>
      <p><strong>Progress:</strong> ${progressText}</p>
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
    date_format: 'YYYY-MM-DD HH:mm:ss',
    custom_popup_html: customPopupHtml,
    language: 'zh',
    start_date: weekStart.value.toDate(),
    end_date: weekEnd.value.toDate(),
    on_click: (task: any) => {
      console.log('Task clicked:', task)
    },
    on_date_change: (task: any, start: string, end: string) => {
      console.log('Date changed:', task, start, end)
    },
    on_progress_change: (task: any, progress: number) => {
      console.log('Progress changed:', task, progress)
    },
    on_view_change: (mode: string) => {
      console.log('View changed:', mode)
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
    0: 'Sunday',
    1: 'Monday',
    2: 'Tuesday',
    3: 'Wednesday',
    4: 'Thursday',
    5: 'Friday',
    6: 'Saturday'
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
