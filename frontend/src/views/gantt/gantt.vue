<template>
  <div class="container">

    <div class="gantt-title" style="text-align: center; color: #5B8FF9;">
      <h2>Task Gantt Chart</h2>
    </div>

    <!-- 甘特图区域 -->
    <div class="gantt-container">
      <div class="gantt-header">
        <!-- 靠左展示 -->
        <div class="gantt-controls" style="text-align: left;">
          <el-button type="primary" @click="fetchTasks" >
            <el-icon><Refresh /></el-icon> Refresh
          </el-button>
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
import { ElMessage } from 'element-plus'
import { getTaskList } from '@/api/task'
import type { TaskDetail } from '@/types/task'

// 计算当前一周的开始（周一）和结束（周日）日期
const weekStart = computed(() => {
  // 从当天开始往前找到最近的周一
  return dayjs().startOf('day').subtract((dayjs().day() === 0 ? 6 : dayjs().day() - 1), 'day')
})
const weekEnd = computed(() => {
  // 从当天开始往后找到最近的周日
  return weekStart.value.add(6, 'day')
})

// 更新甘特图的时间范围
const updateGanttTimeRange = () => {
  if (!ganttChart) {
    console.warn('甘特图尚未初始化，无法更新时间范围')
    return
  }
  
  try {
    // 获取有效的开始和结束日期
    const startDate = weekStart.value.toDate()
    const endDate = weekEnd.value.toDate()
    
    // 确保日期有效
    if (!isValidDate(startDate) || !isValidDate(endDate)) {
      console.error('甘特图日期范围无效:', startDate, endDate)
      ElMessage.error('日期范围错误，无法更新甘特图')
      return
    }
    
    ganttChart.change_view_mode('Day', startDate, endDate)
    
    // 更新后延迟处理表头
    setTimeout(() => {
      customizeHeader()
    }, 0)
  } catch (error) {
    console.error('更新甘特图时间范围失败:', error)
    ElMessage.error('更新甘特图时间范围失败')
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

// 优先级映射
const PRIORITY_MAP = {
  1: 'low',
  2: 'medium',
  3: 'high',
  4: 'urgent'
} as const

// 状态映射
const STATUS_MAP = {
  0: 'pending',
  1: 'in-progress',
  2: 'completed',
  3: 'cancelled'
} as const

/* ---------------------------
   自定义任务详情弹窗样式
--------------------------- */
const customPopupHtml = (task: any) => {
  const priorityMap: Record<string, string> = {
    'urgent': 'Critical',
    'high': 'High',
    'medium': 'Medium',
    'low': 'Low'
  }
  
  const statusMap: Record<string, string> = {
    'pending': 'Pending',
    'in-progress': 'In Progress',
    'completed': 'Completed',
    'cancelled': 'Cancelled'
  }
  
  const priorityText = priorityMap[task.priority] || '未知'
  const statusText = statusMap[task.status] || '未知'
  const progressText = `${task.progress || 0}%`
  const startDate = dayjs(task.start).format('YYYY-MM-DD HH:mm:ss')
  const endDate = dayjs(task.end).format('YYYY-MM-DD HH:mm:ss')
  const description = task.description || '暂无描述'

  // 根据不同优先级设置颜色
  const priorityColors: Record<string, string> = {
    'urgent': '#F56C6C',
    'high': '#FAAD14',
    'medium': '#5B8FF9',
    'low': '#36CFC9'
  }
  const priorityColor = priorityColors[task.priority] || '#5B8FF9'

  return `
    <div class="popup-container" style="border-top-color: ${priorityColor}">
      <h4 class="task-title" style="color: ${priorityColor}">${task.name}</h4>
      <p><strong style="color: ${priorityColor}">Priority:</strong> ${priorityText}</p>
      <p><strong style="color: ${priorityColor}">Status:</strong> ${statusText}</p>
      <p><strong style="color: ${priorityColor}">Progress:</strong> ${progressText}</p>
      <p><strong style="color: ${priorityColor}">Start Date:</strong> ${startDate}</p>
      <p><strong style="color: ${priorityColor}">End Date:</strong> ${endDate}</p>
      <p><strong style="color: ${priorityColor}">Description:</strong> ${description}</p>
    </div>
  `
}

/* ---------------------------
   任务数据和甘特图相关
--------------------------- */
const tasks = ref<TaskDetail[]>([])
const ganttContainer = ref(null)
let ganttChart: any = null

// 验证日期是否有效
const isValidDate = (date: any): boolean => {
  return date instanceof Date && !isNaN(date.getTime())
}

// 从后端获取任务列表
const fetchTasks = async () => {
  try {
    const response = await getTaskList()
    console.log('API返回数据:', response)
    
    // 根据控制台输出，API返回格式为：
    // { code: 1, data: { items: [...], total: 数量 } }
    
    if (response && response.data) {
      let taskItems: TaskDetail[] = []
      
      // 判断返回数据结构
      const responseData = response.data
      
      if (responseData.code === 1 && responseData.data && Array.isArray(responseData.data.items)) {
        // 标准API格式: { code: 1, data: { items: [...] } }
        taskItems = responseData.data.items || []
      } else if (responseData.items && Array.isArray(responseData.items)) {
        // 直接包含items的对象: { items: [...] }
        taskItems = responseData.items
      } else if (Array.isArray(responseData)) {
        // 直接返回数组形式
        taskItems = responseData
      } else {
        console.error('无法识别的任务数据格式:', responseData)
        ElMessage.error('任务数据格式不符合预期')
        taskItems = []
      }
      
      tasks.value = taskItems
      console.log('解析后的任务数据:', tasks.value)
      
      if (ganttChart && tasks.value.length > 0) {
        // 如果甘特图已经初始化且有数据，则更新数据
        updateGanttChart()
      } else if (tasks.value.length > 0) {
        // 如果有任务数据，则初始化甘特图
        initGantt()
      } else {
        console.warn('没有任务数据可显示')
        ElMessage.warning('没有任务数据可显示')
      }
      
      // 添加延时强制刷新，确保样式正确应用
      setTimeout(() => {
        if (ganttChart) {
          updateGanttTimeRange()
          customizeHeader()
        }
      }, 200)
    } else {
      console.error('API返回数据格式错误')
      ElMessage.error('获取任务数据失败')
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  }
}

// 将任务数据转换为甘特图所需格式
const convertTasksToGanttFormat = () => {
  if (!tasks.value || !Array.isArray(tasks.value)) {
    console.error('任务数据格式错误:', tasks.value)
    return []
  }
  
  // 保证开始日期和结束日期都是有效日期
  const defaultStartDate = new Date()
  const defaultEndDate = dayjs(defaultStartDate).add(1, 'day').toDate()
  
  return tasks.value.map(task => {
    // 后端返回的实际数据结构:
    // { id, name, description, projectId, status, priority, startTime, deadline }
    
    // 确保有效的ID
    const id = String(task.id || '')
    
    // 任务名称
    const name = task.name || `任务 #${task.id || ''}`
    
    // 使用任务的实际开始时间和截止时间
    let startTime: Date
    let endTime: Date
    
    // 尝试解析startTime
    if (task.startTime) {
      startTime = new Date(task.startTime)
      if (!isValidDate(startTime)) {
        console.warn(`任务 #${id} 的开始时间无效:`, task.startTime)
        startTime = defaultStartDate
      }
    } else {
      startTime = defaultStartDate
    }
    
    // 尝试解析deadline
    if (task.deadline) {
      endTime = new Date(task.deadline)
      if (!isValidDate(endTime)) {
        console.warn(`任务 #${id} 的截止时间无效:`, task.deadline)
        endTime = defaultEndDate
      }
    } else {
      endTime = defaultEndDate
    }
    
    // 如果开始时间晚于结束时间，使用默认结束时间
    if (startTime > endTime) {
      console.warn(`任务 #${id} 的开始时间晚于截止时间，使用默认截止时间`)
      endTime = dayjs(startTime).add(1, 'day').toDate()
    }
    
    // 计算任务进度
    let progress = 0
    if (task.status === 2) { // 已完成
      progress = 100
    } else if (task.status === 1) { // 进行中
      progress = 50
    }
    
    // 确定状态 - 基于状态区分任务
    let statusValue = 'pending'
    if (typeof task.status === 'number') {
      if (task.status === 0) statusValue = 'pending'
      else if (task.status === 1) statusValue = 'in-progress'
      else if (task.status === 2) statusValue = 'completed'
      else if (task.status === 3) statusValue = 'cancelled'
    }
    
    // 确定优先级 - 使用推荐的色系
    let priorityClass = 'medium'
    if (typeof task.priority === 'number') {
      if (task.priority === 1) priorityClass = 'low'
      else if (task.priority === 2) priorityClass = 'medium'
      else if (task.priority === 3) priorityClass = 'high'
      else if (task.priority === 4) priorityClass = 'urgent'
    }
    
    return {
      id: id,
      name: name,
      description: task.description || '',
      projectId: task.projectId, // 保留项目ID信息用于内部逻辑
      start: startTime,
      end: endTime,
      progress: progress,
      dependencies: '',
      custom_class: `priority-${priorityClass} status-${statusValue}`, // 同时使用优先级和状态类
      priority: priorityClass,
      status: statusValue
    }
  })
}

// 更新甘特图数据
const updateGanttChart = () => {
  if (!ganttChart) {
    console.warn('甘特图尚未初始化，无法更新')
    initGantt() // 尝试初始化
    return
  }
  
  try {
    const ganttTasks = convertTasksToGanttFormat()
    
    // 确保有任务数据
    if (!ganttTasks || ganttTasks.length === 0) {
      console.warn('没有可显示的任务数据')
      return
    }
    
    ganttChart.refresh(ganttTasks)
    
    // 增加延迟确保DOM完全渲染
    setTimeout(() => {
      customizeHeader()
    }, 100)
  } catch (error) {
    console.error('更新甘特图失败:', error)
    ElMessage.error('更新甘特图失败')
  }
}

// 初始化甘特图
const initGantt = () => {
  if (!ganttContainer.value) return
  
  try {
    const ganttTasks = convertTasksToGanttFormat()
    
    // 确保有任务数据
    if (!ganttTasks || ganttTasks.length === 0) {
      console.warn('没有可显示的任务数据')
      return
    }
    
    // 确保所有任务都有有效的开始和结束日期
    const validTasks = ganttTasks.filter(task => 
      isValidDate(task.start) && 
      isValidDate(task.end)
    )
    
    if (validTasks.length === 0) {
      console.error('没有包含有效日期的任务')
      ElMessage.error('任务日期格式错误，无法显示甘特图')
      return
    }
    
    // 获取今天的日期
    const today = dayjs().startOf('day')
    // 确保显示当前日期，计算合适的开始和结束日期
    const startDate = today.subtract(2, 'day').toDate()
    const endDate = today.add(10, 'day').toDate()
    
    // 确保日期有效
    if (!isValidDate(startDate) || !isValidDate(endDate)) {
      console.error('甘特图日期范围无效:', startDate, endDate)
      ElMessage.error('日期范围错误，无法显示甘特图')
      return
    }
    
    ganttChart = new Gantt(ganttContainer.value, validTasks, {
      header_height: 60,
      column_width: 120,
      step: 24,
      view_modes: ['Day', 'Week', 'Month'],
      bar_height: 40,
      bar_corner_radius: 3,
      arrow_curve: 5,
      padding: 18,
      view_mode: 'Day',
      date_format: 'YYYY-MM-DD HH:mm:ss',
      custom_popup_html: customPopupHtml,
      language: 'en', // 修改为英文
      start_date: startDate,
      end_date: endDate,
      on_click: (task: any) => {
        console.log('任务点击:', task)
      },
      on_date_change: (task: any, start: string, end: string) => {
        console.log('日期变更:', task, start, end)
      },
      on_progress_change: (task: any, progress: number) => {
        console.log('进度变更:', task, progress)
      },
      on_view_change: (mode: string) => {
        console.log('视图变更:', mode)
        setTimeout(() => {
          customizeHeader()
        }, 50) // 增加延迟确保DOM渲染完成
      }
    })

    // 增加延迟确保DOM完全渲染
    setTimeout(() => {
      customizeHeader()
    }, 100)
  } catch (error) {
    console.error('初始化甘特图失败:', error)
    ElMessage.error('初始化甘特图失败')
  }
}

// 自定义更新表头，将每个 header cell 修改为两行显示：星期和日期
const customizeHeader = () => {
  try {
    console.log('开始自定义表头...')
    const headerCells = document.querySelectorAll('.gantt .grid-header .grid-header-cell')
    if (!headerCells || headerCells.length === 0) {
      console.warn('未找到甘特图表头元素')
      return
    }
    
    // 获取今天的日期，用于高亮显示
    const today = dayjs().format('YYYY-MM-DD')
    console.log('今天日期:', today)
    
    headerCells.forEach((cell: Element, index: number) => {
      if (!cell) return
      
      const cellElement = cell as HTMLElement
      const dateText = cellElement.innerText ? cellElement.innerText.trim() : ''
      
      if (!dateText) {
        console.warn('表头单元格文本为空')
        return
      }
      
      try {
        const dateObj = dayjs(dateText)
        if (dateObj.isValid()) {
          const weekday = getWeekdayLabel(dateObj)
          const formattedDate = dateObj.format('MMM DD') // 英文月份显示
          
          // 如果是当天，添加特殊标记，使用英文
          const isToday = dateObj.format('YYYY-MM-DD') === today
          const todayMark = isToday ? '<span class="today-mark">Today</span>' : ''
          
          cellElement.innerHTML = `
            <div class="date-header">
              <div class="weekday">${weekday} ${todayMark}</div>
              <div class="date">${formattedDate}</div>
            </div>
          `
          
          // 如果是当天，添加高亮类
          if (isToday) {
            console.log('找到今天的单元格:', index, dateObj.format('YYYY-MM-DD'))
            // 找到对应的列并添加高亮样式
            const columnIndex = Array.from(headerCells).indexOf(cell)
            if (columnIndex !== -1) {
              // 给甘特图中对应的时间列添加高亮样式
              const columns = document.querySelectorAll('.gantt .grid-row .grid-cell')
              const columnsPerRow = headerCells.length
              
              for (let i = 0; i < columns.length; i++) {
                if (i % columnsPerRow === columnIndex) {
                  (columns[i] as HTMLElement).classList.add('today-highlight')
                }
              }
              
              // 同时高亮表头单元格
              cellElement.classList.add('today-highlight')
            }
          }
        } else {
          console.warn('无效的日期文本:', dateText)
        }
      } catch (error) {
        console.error('处理表头日期时出错:', error)
      }
    })
    
    // 自动滚动到今天日期
    setTimeout(() => {
      scrollToToday()
    }, 50)
  } catch (error) {
    console.error('自定义表头处理失败:', error)
  }
}

// 添加自动滚动到今天的功能
const scrollToToday = () => {
  try {
    console.log('尝试滚动到今天日期')
    const todayElement = document.querySelector('.gantt .grid-header .today-highlight')
    if (todayElement) {
      console.log('找到今天的元素', todayElement)
      const ganttEl = document.querySelector('.gantt')
      if (ganttEl) {
        console.log('找到甘特图元素', ganttEl)
        // 计算滚动位置，使今天居中
        const todayRect = todayElement.getBoundingClientRect()
        const ganttRect = ganttEl.getBoundingClientRect()
        const scrollLeft = todayRect.left - ganttRect.left - (ganttRect.width / 2) + (todayRect.width / 2)
        
        console.log('滚动位置计算:', {
          todayLeft: todayRect.left,
          ganttLeft: ganttRect.left,
          ganttWidth: ganttRect.width,
          todayWidth: todayRect.width,
          scrollLeft: scrollLeft
        })
        
        // 平滑滚动到今天
        ganttEl.scrollLeft = scrollLeft > 0 ? scrollLeft : 0
        console.log('设置滚动位置:', ganttEl.scrollLeft)
      }
    } else {
      console.warn('未找到今天的高亮元素')
    }
  } catch (error) {
    console.error('滚动到今天失败:', error)
  }
}

// 获取星期几
const getWeekdayLabel = (dateObj: dayjs.Dayjs) => {
  const weekDayMap: Record<number, string> = {
    0: 'Sun',
    1: 'Mon',
    2: 'Tue',
    3: 'Wed',
    4: 'Thu',
    5: 'Fri',
    6: 'Sat'
  }
  return weekDayMap[dateObj.day()] || ''
}

onMounted(() => {
  fetchTasks() // 组件挂载后获取任务数据
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

.gantt-title h2 {
  margin-bottom: 20px;
  color: #5B8FF9;
  font-weight: 500;
}

/* 甘特图区域样式 */
.gantt-container {
  margin-top: 16px;
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

/* 优先级相关样式 - 应用推荐的颜色系统 */
.gantt .bar.priority-urgent {
  fill: #F56C6C; /* 紧急 - 深红色 */
}

.gantt .bar.priority-high {
  fill: #FAAD14; /* 高 - 阿里系黄色 */
}

.gantt .bar.priority-medium {
  fill: #5B8FF9; /* 中 - AntV蓝色 */
}

.gantt .bar.priority-low {
  fill: #36CFC9; /* 低 - 绿色+青蓝 */
}

/* 状态修饰 */
.gantt .bar.status-completed {
  fill-opacity: 0.9;
  stroke: #67C23A;
  stroke-width: 1px;
}

.gantt .bar.status-in-progress {
  fill-opacity: 0.9;
}

.gantt .bar.status-pending {
  fill-opacity: 0.7;
}

.gantt .bar.status-cancelled {
  fill-opacity: 0.4;
  stroke: #909399;
  stroke-width: 1px;
  stroke-dasharray: 4;
}

/* 修改进度条颜色为系统蓝色 */
.gantt .bar-progress {
  fill: #5B8FF9 !important; /* 使用系统标准蓝色 */
}

/* 鼠标悬停时进度条颜色 */
.gantt .bar-wrapper:hover .bar-progress {
  fill: #4a7fe0 !important; /* 稍深一点的蓝色 */
}

.gantt .bar-wrapper.active .bar-progress {
  fill: #4a7fe0 !important; /* 稍深一点的蓝色 */
}

/* 表头样式 */
.gantt .date-header {
  text-align: center;
  line-height: 1.4;
}

.gantt .date-header .weekday {
  font-weight: bold;
  color: #5B8FF9;
}

.gantt .date-header .date {
  font-size: 12px;
  color: #8c9fba;
}

.gantt .date-header .today-mark {
  display: inline-block;
  padding: 0 4px;
  background-color: #FAAD14; /* 使用黄色背景 */
  color: white;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 4px;
  font-weight: bold;
}

/* 今日高亮样式 */
.gantt .today-highlight {
  background-color: #FFF7E6 !important; /* 鲜艳的黄色背景 */
  border-left: 4px solid #FAAD14 !important; /* 加粗黄色边框 */
  border-right: 4px solid #FAAD14 !important; /* 右侧也加粗边框 */
  position: relative;
  z-index: 1;
}

/* 今日单元格表头特殊样式 */
.gantt .grid-header .today-highlight {
  background-color: #FFDA85 !important; /* 表头使用更明显的黄色 */
}

.gantt .grid-header .today-highlight .weekday {
  color: #D46B08 !important; /* 更改文字颜色为深橙色 */
  font-weight: 800;
}

.gantt .grid-header .today-highlight .date {
  color: #D46B08 !important; /* 日期也使用深橙色 */
  font-weight: bold;
}

/* 弹窗样式 */
.popup-container {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-width: 200px;
  border-top: 3px solid #5B8FF9;
}

.popup-container .task-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #5B8FF9;
  font-weight: 500;
}

.popup-container p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.popup-container p strong {
  color: #5B8FF9;
  margin-right: 8px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .gantt-chart {
    height: 300px;
  }
}
</style>
