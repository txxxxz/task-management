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
          
          <el-label style="font-size: 14px; ">Show Completed Tasks</el-label>
          <!-- 添加显示已完成任务的开关 -->
          <el-switch
            v-model="showCompletedTasks"
            active-text="Show"
            inactive-text="Hide"
            inline-prompt
            class="task-switch"
            @change="updateGanttWithTaskFilter"
          />
        </div>
      </div>
      
      <!-- 甘特图挂载容器 -->
      <div ref="ganttContainer" class="gantt-chart"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, onUnmounted, onActivated } from 'vue'
import { Search, Refresh, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import Gantt from 'frappe-gantt'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getTaskList } from '@/api/task'
import type { TaskDetail } from '@/types/task'
import { useUserStore } from '@/stores/user'

// 是否显示已完成的任务
const showCompletedTasks = ref(false)

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
  const description = task.description || 'No description'

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
      <p><strong style="color: ${priorityColor}">Start Time:</strong> ${startDate}</p>
      <p><strong style="color: ${priorityColor}">End Time:</strong> ${endDate}</p>
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
      
      // 添加详细的字段调试信息
      if (tasks.value.length > 0) {
        const sampleTask = tasks.value[0];
        console.log('示例任务字段结构:', {
          id: sampleTask.id,
          name: sampleTask.name,
          startTime: sampleTask.startTime,
          deadline: sampleTask.deadline,
          dueTime: sampleTask.dueTime,
          status: sampleTask.status,
          priority: sampleTask.priority,
          所有字段: Object.keys(sampleTask)
        });
      }
      
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
    
    // 尝试解析deadline - 同时检查deadline和dueTime字段
    if (task.deadline) {
      endTime = new Date(task.deadline)
      if (!isValidDate(endTime)) {
        console.warn(`任务 #${id} 的截止时间(deadline)无效:`, task.deadline)
        endTime = defaultEndDate
      }
    } else if (task.dueTime) {
      // 增加对dueTime字段的支持
      endTime = new Date(task.dueTime)
      if (!isValidDate(endTime)) {
        console.warn(`任务 #${id} 的截止时间(dueTime)无效:`, task.dueTime)
        endTime = defaultEndDate
      }
    } else {
      console.log(`任务 #${id} 没有设置截止时间，使用默认值`)
      endTime = defaultEndDate
    }
    
    // 添加日志，方便调试
    console.log(`任务 #${id} 的日期信息:`, {
      原始开始时间: task.startTime,
      原始截止时间deadline: task.deadline,
      原始截止时间dueTime: task.dueTime,
      处理后开始时间: startTime,
      处理后结束时间: endTime
    });
    
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
      // 确保进度条正确显示
      ensureProgressBarDisplay()
    }, 100)
  } catch (error) {
    console.error('更新甘特图失败:', error)
    ElMessage.error('更新甘特图失败')
  }
}

// 自定义更新表头，将每个 header cell 修改为两行显示：星期和日期
const customizeHeader = () => {
  try {
    console.log('开始自定义表头...')
    
    // 增加延迟确保DOM完全渲染
    setTimeout(() => {
      // 尝试多种可能的选择器找到表头单元格
      const selectors = [
        '.gantt .grid-header .grid-header-cell',
        '.gantt .grid-header-cell',
        '.gantt .grid-row .grid-header-cell',
        '.gantt [class*="header"] [class*="cell"]',
        '.gantt svg .tick'
      ]
      
      let headerCells = null
      let usedSelector = ''
      
      // 遍历尝试选择器
      for (const selector of selectors) {
        const cells = document.querySelectorAll(selector)
        if (cells && cells.length > 0) {
          headerCells = cells
          usedSelector = selector
          console.log(`找到表头元素，使用选择器: ${usedSelector}，数量: ${cells.length}`)
          break
        }
      }
      
      if (!headerCells || headerCells.length === 0) {
        console.warn('所有选择器都未找到甘特图表头元素，跳过自定义表头处理')
        return
      }
      
      // 获取今天的日期，用于高亮显示
      const today = dayjs().format('YYYY-MM-DD')
      console.log('今天日期:', today)
      
      // 先移除所有已有的today-highlight类，防止重复添加
      document.querySelectorAll('.today-highlight').forEach(element => {
        element.classList.remove('today-highlight')
      })
      
      // 记录是否找到今天日期
      let foundToday = false
      
      headerCells.forEach((cell: Element, index: number) => {
        if (!cell) return
        
        const cellElement = cell as HTMLElement
        const dateText = cellElement.innerText ? cellElement.innerText.trim() : ''
        
        if (!dateText) {
          // 对于空文本的情况，可能是svg中的tick元素
          if (usedSelector.includes('tick')) {
            try {
              // 尝试从SVG tick元素中获取日期信息
              const transform = cellElement.getAttribute('transform')
              if (transform) {
                // 对于SVG tick，位置可能和日期相关
                const match = transform.match(/translate\((\d+)/)
                if (match && match[1]) {
                  const position = parseInt(match[1])
                  console.log(`SVG tick位置: ${position}`)
                }
              }
            } catch (e) {
              console.warn('处理SVG tick元素出错:', e)
            }
          }
          return
        }
        
        try {
          // 保存原始日期文本到data-date属性，用于后续比较
          const originalDate = dateText
          cellElement.setAttribute('data-date', originalDate)
          
          // 尝试解析日期
          let dateObj = dayjs(originalDate)
          
          // 如果解析失败，尝试使用其他格式
          if (!dateObj.isValid()) {
            // 可能只有日期数字，如"26"
            if (/^\d{1,2}$/.test(dateText)) {
              // 假设是当前月份的某一天
              const currentMonth = dayjs().month()
              const currentYear = dayjs().year()
              dateObj = dayjs(new Date(currentYear, currentMonth, parseInt(dateText)))
            } else {
              // 尝试其他常见格式
              const formats = ['MMM DD', 'DD MMM', 'M/D', 'D/M', 'YYYY-MM-DD', 'MM/DD']
              for (const format of formats) {
                dateObj = dayjs(dateText, format)
                if (dateObj.isValid()) break
              }
            }
          }
          
          // 如果所有尝试都失败，记录错误并继续
          if (!dateObj.isValid()) {
            console.warn('无法解析日期文本:', dateText)
            return
          }
          
          const weekday = getWeekdayLabel(dateObj)
          const formattedDate = dateObj.format('MMM DD') // 英文月份显示
          
          // 将原始日期格式保存为YYYY-MM-DD格式，用于比较
          const dateForComparison = dateObj.format('YYYY-MM-DD')
          cellElement.setAttribute('data-full-date', dateForComparison)
          
          // 如果是当天，添加特殊标记，使用英文
          const isToday = dateForComparison === today
          const todayMark = isToday ? '<span class="today-mark">Today</span>' : ''
          
          cellElement.innerHTML = `
            <div class="date-header">
              <div class="weekday">${weekday} ${todayMark}</div>
              <div class="date">${formattedDate}</div>
            </div>
          `
          
          // 如果是当天，添加高亮类，并记录详细日志
          if (isToday) {
            console.log('找到今天的单元格:', index, dateForComparison)
            foundToday = true
            
            // 明确给当前单元格添加today-highlight类
            cellElement.classList.add('today-highlight')
            
            // 找到对应的列并添加高亮样式
            const columnIndex = Array.from(headerCells).indexOf(cell)
            if (columnIndex !== -1) {
              console.log('今天列的索引:', columnIndex)
              
              // 给甘特图中对应的时间列添加高亮样式
              // 尝试多种方式查找对应的列
              try {
                // 方法1: 使用grid-cell
                const columns = document.querySelectorAll('.gantt .grid-row .grid-cell')
                if (columns && columns.length > 0) {
                  const columnsPerRow = headerCells.length
                  
                  let highlightedCount = 0
                  for (let i = 0; i < columns.length; i++) {
                    if (i % columnsPerRow === columnIndex) {
                      const column = columns[i] as HTMLElement
                      column.classList.add('today-highlight')
                      highlightedCount++
                    }
                  }
                  
                  console.log(`成功高亮了${highlightedCount}个单元格`)
                }
                
                // 方法2: 尝试查找SVG中的矩形元素
                const svgRects = document.querySelectorAll('.gantt svg rect')
                if (svgRects && svgRects.length > 0) {
                  // 计算今天矩形元素的大致x坐标
                  // 假设单元格宽度固定，则x位置应该与索引成比例
                  const firstRect = svgRects[0] as SVGRectElement
                  const lastRect = svgRects[svgRects.length - 1] as SVGRectElement
                  const totalWidth = parseFloat(lastRect.getAttribute('x') || '0') - parseFloat(firstRect.getAttribute('x') || '0')
                  const cellWidth = totalWidth / (headerCells.length || 1)
                  const todayX = parseFloat(firstRect.getAttribute('x') || '0') + (columnIndex * cellWidth)
                  
                  let svgHighlightCount = 0
                  svgRects.forEach((rect, idx) => {
                    const rectX = parseFloat(rect.getAttribute('x') || '0')
                    // 如果矩形的x位置接近今天的计算位置，添加今日高亮
                    if (Math.abs(rectX - todayX) < cellWidth / 2) {
                      // 创建一个今日高亮矩形
                      const svgRect = rect as SVGElement
                      if (!svgRect.classList.contains('today-highlight')) {
                        svgRect.classList.add('today-highlight')
                        svgHighlightCount++
                      }
                    }
                  })
                  
                  console.log(`成功高亮了${svgHighlightCount}个SVG矩形`)
                }
              } catch (e) {
                console.error('添加列高亮时出错:', e)
              }
            }
          }
        } catch (error) {
          console.error('处理表头日期时出错:', error)
        }
      })
      
      if (!foundToday) {
        console.warn('未在表头中找到今天的日期，将尝试其他方式高亮今天')
        // 尝试直接给SVG中的某些元素添加今日高亮
        // 这是一个备选方案，直接查找包含今天日期的SVG元素
        try {
          // 注入一个today-highlight元素
          const svgContainer = document.querySelector('.gantt svg')
          if (svgContainer) {
            // 查找SVG元素的宽高
            const width = parseInt(svgContainer.getAttribute('width') || '800')
            const height = parseInt(svgContainer.getAttribute('height') || '400')
            
            // 计算今天在时间轴上的大致位置
            // 这里假设gantt图表示一个月的数据，将时间轴均分
            const daysInMonth = dayjs().daysInMonth()
            const dayOfMonth = dayjs().date()
            const percentageOfMonth = dayOfMonth / daysInMonth
            const xPosition = Math.floor(width * percentageOfMonth)
            
            // 创建今日高亮元素
            const todayHighlight = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
            todayHighlight.setAttribute('class', 'today-highlight')
            todayHighlight.setAttribute('x', `${xPosition - 2}`)
            todayHighlight.setAttribute('y', '0')
            todayHighlight.setAttribute('width', '4')
            todayHighlight.setAttribute('height', `${height}`)
            todayHighlight.setAttribute('fill', 'rgba(24, 144, 255, 0.15)')
            todayHighlight.setAttribute('stroke', '#1890ff')
            todayHighlight.setAttribute('stroke-width', '2')
            
            svgContainer.appendChild(todayHighlight)
            console.log('手动添加了今日高亮SVG元素')
          }
        } catch (e) {
          console.error('尝试手动添加今日高亮失败:', e)
        }
      }
      
      // 自动滚动到今天日期
      setTimeout(() => {
        scrollToToday()
      }, 100)
    }, 200) // 增加延迟确保DOM渲染完成
  } catch (error) {
    console.error('自定义表头处理总体失败:', error)
  }
}

// 改进自动滚动到今天的功能
const scrollToToday = () => {
  try {
    console.log('尝试滚动到今天日期')
    
    // 获取今天的日期字符串
    const today = dayjs().format('YYYY-MM-DD')
    
    // 先尝试直接用今天的高亮元素滚动方法
    const todayElement = document.querySelector('.gantt .today-highlight')
    if (todayElement) {
      console.log('找到今天的元素，使用scrollIntoView方法')
      // 使用scrollIntoView方法更可靠地滚动到今天
      todayElement.scrollIntoView({ 
        inline: 'center',  // 水平居中
        behavior: 'smooth' // 平滑滚动
      })
      return // 如果成功执行，直接返回
    }
    
    // 如果上面的方法失败，尝试原来的方法
    // 首先尝试通过data-full-date属性查找今天的元素
    let todayHeaderElement = null
    const headerCells = document.querySelectorAll('.gantt .grid-header .grid-header-cell')
    
    for (let i = 0; i < headerCells.length; i++) {
      const cell = headerCells[i] as HTMLElement
      const fullDate = cell.getAttribute('data-full-date')
      
      if (fullDate === today) {
        todayHeaderElement = cell
        console.log('通过data-full-date找到今天的元素:', fullDate)
        break
      }
    }
    
    if (todayHeaderElement) {
      // 查找正确的滚动容器
      // frappe-gantt使用.gantt .scroll-container作为滚动容器
      const scrollContainers = [
        document.querySelector('.gantt .scroll-container'),
        document.querySelector('.gantt .grid'),
        document.querySelector('.gantt')
      ]
      
      // 使用第一个找到的有效容器
      const scrollContainer = scrollContainers.find(el => el !== null)
      
      if (scrollContainer) {
        console.log('找到滚动容器:', scrollContainer)
        
        // 使用requestAnimationFrame确保DOM已完全渲染
        requestAnimationFrame(() => {
          // 计算滚动位置，使今天居中
          const todayRect = todayHeaderElement!.getBoundingClientRect()
          const containerRect = scrollContainer.getBoundingClientRect()
          
          // 获取甘特图左侧边栏的宽度
          const sidebarWidth = document.querySelector('.gantt .grid-header-row')?.clientWidth || 0
          
          // 今天的列与容器左边的距离（考虑可能存在的左侧边栏）
          const offsetFromContainerLeft = todayRect.left - containerRect.left
          
          // 滚动到的位置：使今天居中
          const scrollTo = Math.max(0, offsetFromContainerLeft - (containerRect.width / 2) + (todayRect.width / 2))
          
          console.log('滚动位置计算:', {
            todayLeft: todayRect.left,
            containerLeft: containerRect.left,
            containerWidth: containerRect.width,
            todayWidth: todayRect.width,
            offsetFromContainerLeft,
            sidebarWidth,
            scrollTo
          })
          
          // 尝试两种滚动方法
          // 1. 使用scrollLeft属性
          scrollContainer.scrollLeft = scrollTo
          
          // 2. 如果第一种方法不奏效，使用scrollTo方法
          setTimeout(() => {
            if (Math.abs(scrollContainer.scrollLeft - scrollTo) > 5) {
              console.log('使用scrollTo方法尝试滚动')
              try {
                scrollContainer.scrollTo({
                  left: scrollTo,
                  behavior: 'smooth'
                })
              } catch (e) {
                console.error('scrollTo方法失败:', e)
              }
            }
          }, 50)
        })
      } else {
        console.warn('未找到有效的滚动容器')
      }
    } else {
      console.warn('未找到今天的日期元素')
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

// 动态注入样式，特别针对SVG元素使用fill和stroke
const injectTodayHighlightStyles = () => {
  // 移除可能已存在的样式元素
  const existingStyles = document.getElementById('custom-gantt-today-styles')
  if (existingStyles) {
    existingStyles.remove()
  }
  
  // 创建新的样式元素，包含任务条优先级颜色样式和SVG属性
  const styleElement = document.createElement('style')
  styleElement.id = 'custom-gantt-today-styles'
  styleElement.innerHTML = `
    /* 今日高亮SVG元素样式 - 使用深蓝色系 */
    .gantt rect.today-highlight {
      fill: rgba(24, 144, 255, 0.15) !important; /* 淡蓝色背景 */
      stroke: #1890ff !important; /* 蓝色边框 */
      stroke-width: 2px !important; /* 边框宽度 */
      filter: drop-shadow(0 0 3px rgba(24, 144, 255, 0.4)) !important; /* 蓝色阴影效果 */
    }
    
    /* 表头样式（非SVG部分） */
    .gantt .grid-header .grid-header-cell.today-highlight,
    .gantt .grid-header .today-highlight {
      background-color: rgba(24, 144, 255, 0.15) !important; /* 淡蓝色背景 */
      border-top: 3px solid #1890ff !important; /* 蓝色顶部边框 */
      box-shadow: 0 4px 8px rgba(24, 144, 255, 0.2) !important; /* 柔和阴影 */
    }
    
    /* 表头内的文字样式 */
    .gantt .grid-header .today-highlight .weekday,
    .gantt .grid-header .grid-header-cell.today-highlight .weekday,
    .gantt .grid-header .today-highlight .date,
    .gantt .grid-header .grid-header-cell.today-highlight .date {
      color: #1890ff !important; /* 蓝色文字 */
      font-weight: 800 !important;
    }
    
    /* Today标签 */
    .gantt .date-header .today-mark {
      display: inline-block !important;
      padding: 0 6px !important;
      background-color: #1890ff !important; /* 蓝色背景 */
      color: white !important;
      border-radius: 4px !important;
      font-size: 12px !important;
      margin-left: 4px !important;
      font-weight: bold !important;
      box-shadow: 0 2px 4px rgba(24, 144, 255, 0.4) !important;
    }
    
    /* 优先级颜色 - 直接针对SVG元素设置fill属性 */
    .gantt .bar-wrapper.priority-urgent .bar {
      fill: #F56C6C !important; /* 紧急 - 深红色 */
    }
    
    .gantt .bar-wrapper.priority-high .bar {
      fill: #FAAD14 !important; /* 高 - 阿里系黄色 */
    }
    
    .gantt .bar-wrapper.priority-medium .bar {
      fill: #5B8FF9 !important; /* 中 - AntV蓝色 */
    }
    
    .gantt .bar-wrapper.priority-low .bar {
      fill: #36CFC9 !important; /* 低 - 绿色+青蓝 */
    }
    
    /* 进度条颜色匹配任务颜色 */
    .gantt .bar-wrapper.priority-urgent .bar-progress {
      fill: #F56C6C !important;
      stroke: #F56C6C !important;
      opacity: 1 !important;
      visibility: visible !important;
    }
    
    .gantt .bar-wrapper.priority-high .bar-progress {
      fill: #FAAD14 !important;
      stroke: #FAAD14 !important;
      opacity: 1 !important;
      visibility: visible !important;
    }
    
    .gantt .bar-wrapper.priority-medium .bar-progress {
      fill: #5B8FF9 !important;
      stroke: #5B8FF9 !important;
      opacity: 1 !important;
      visibility: visible !important;
    }
    
    .gantt .bar-wrapper.priority-low .bar-progress {
      fill: #36CFC9 !important;
      stroke: #36CFC9 !important;
      opacity: 1 !important;
      visibility: visible !important;
    }
    
    /* 进度条增强可见性 */
    .gantt .bar-wrapper .bar-progress {
      fill-opacity: 0.85 !important;
      stroke-width: 0.5 !important;
      opacity: 1 !important;
      visibility: visible !important;
      display: block !important;
    }
  `
  
  // 将样式添加到文档末尾
  document.head.appendChild(styleElement)
  console.log('动态注入SVG今日高亮样式和任务优先级颜色样式')
}

// 更新甘特图，应用任务过滤
const updateGanttWithTaskFilter = () => {
  if (!ganttChart) {
    console.warn('甘特图尚未初始化')
    return
  }
  
  try {
    // 获取所有任务
    const allTasks = convertTasksToGanttFormat()
    
    // 根据开关状态决定是否过滤掉已完成任务
    const filteredTasks = showCompletedTasks.value 
      ? allTasks 
      : allTasks.filter(task => task.status !== 'completed')
    
    console.log('甘特图任务过滤:', {
      显示已完成任务: showCompletedTasks.value,
      总任务数: allTasks.length,
      显示任务数: filteredTasks.length,
      已过滤完成任务数: allTasks.length - filteredTasks.length
    })
    
    // 更新甘特图数据
    if (filteredTasks.length > 0) {
      ganttChart.refresh(filteredTasks)
      
      // 延迟处理表头和定位到今天
      setTimeout(() => {
        customizeHeader()
        scrollToToday()
      }, 100)
    } else {
      console.warn('过滤后没有可显示的任务')
      ElMessage.warning('No tasks to display after filtering')
    }
  } catch (error) {
    console.error('更新甘特图任务过滤失败:', error)
    ElMessage.error('Failed to update task filter')
  }
}

// 初始化甘特图
const initGantt = () => {
  if (!ganttContainer.value) return
  
  try {
    // 获取所有任务
    const allTasks = convertTasksToGanttFormat()
    
    // 根据开关状态决定是否过滤掉已完成任务
    const ganttTasks = showCompletedTasks.value 
      ? allTasks 
      : allTasks.filter(task => task.status !== 'completed')
    
    console.log('甘特图任务数量:', {
      显示已完成任务: showCompletedTasks.value,
      总任务数: allTasks.length,
      显示任务数: ganttTasks.length,
      已过滤完成任务数: allTasks.length - ganttTasks.length
    })
    
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
    
    // 获取今天的日期和所有任务的日期范围
    const today = dayjs().startOf('day')
    
    // 获取任务的最早和最晚日期
    let earliestDate = today.toDate()
    let latestDate = today.add(1, 'month').toDate()
    
    // 如果有任务数据，计算其日期范围
    if (validTasks.length > 0) {
      validTasks.forEach(task => {
        if (task.start && task.start < earliestDate) {
          earliestDate = task.start
        }
        if (task.end && task.end > latestDate) {
          latestDate = task.end
        }
      })
    }
    
    // 确保日期范围包含今天
    // 如果最早日期比今天晚，使用今天作为开始
    if (dayjs(earliestDate).isAfter(today)) {
      earliestDate = today.toDate()
    }
    
    // 如果最晚日期比今天+1个月早，使用今天+1个月作为结束
    if (dayjs(latestDate).isBefore(today.add(1, 'month'))) {
      latestDate = today.add(1, 'month').toDate()
    }
    
    // 扩展日期范围，确保图表左侧有足够空间显示今天
    const startDate = dayjs(earliestDate).subtract(5, 'day').toDate()
    const endDate = dayjs(latestDate).add(5, 'day').toDate()
    
    console.log('甘特图时间范围:', {
      today: today.format('YYYY-MM-DD'),
      earliestTaskDate: dayjs(earliestDate).format('YYYY-MM-DD'),
      latestTaskDate: dayjs(latestDate).format('YYYY-MM-DD'),
      startDate: dayjs(startDate).format('YYYY-MM-DD'),
      endDate: dayjs(endDate).format('YYYY-MM-DD')
    })
    
    // 确保日期有效
    if (!isValidDate(startDate) || !isValidDate(endDate)) {
      console.error('甘特图日期范围无效:', startDate, endDate)
      ElMessage.error('日期范围错误，无法显示甘特图')
      return
    }
    
    ganttChart = new Gantt(ganttContainer.value, validTasks, {
      header_height: 60,
      column_width: 30, // 减小列宽，使一屏能显示更多天
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
          injectTodayHighlightStyles() // 在视图变更后重新注入SVG样式
        }, 100) // 增加延迟确保DOM渲染完成
      }
    })

    // 增加延迟确保DOM完全渲染后执行滚动
    // 使用多个延迟尝试，确保在DOM完全渲染后执行
    const delays: number[] = [300, 600, 1000]
    delays.forEach((delay: number) => {
      setTimeout(() => {
        customizeHeader()
        injectTodayHighlightStyles() // 每次都注入SVG样式
        ensureProgressBarDisplay() // 确保进度条正确显示
        // 每次自定义表头后尝试滚动到今天
        setTimeout(() => {
          scrollToToday()
        }, 100)
      }, delay)
    })
  } catch (error) {
    console.error('初始化甘特图失败:', error)
    ElMessage.error('初始化甘特图失败')
  }
}

// 确保进度条正确显示
const ensureProgressBarDisplay = () => {
  try {
    console.log('确保进度条正确显示...')
    
    // 获取所有进度条元素
    const progressBars = document.querySelectorAll('.gantt .bar-wrapper .bar-progress')
    
    if (!progressBars || progressBars.length === 0) {
      console.warn('未找到任何进度条元素')
      return
    }
    
    console.log(`找到 ${progressBars.length} 个进度条元素`)
    
    // 遍历所有进度条并确保其正确显示
    progressBars.forEach((progressElement: Element) => {
      const progressBar = progressElement as SVGElement
      
      // 确保元素可见
      progressBar.style.display = 'block'
      progressBar.style.visibility = 'visible'
      progressBar.style.opacity = '1'
      
      // 获取父元素
      const barWrapper = progressBar.closest('.bar-wrapper')
      if (!barWrapper) return
      
      // 获取任务的优先级信息
      let priority = 'medium'
      for (const className of barWrapper.classList) {
        if (className.startsWith('priority-')) {
          priority = className.replace('priority-', '')
          break
        }
      }
      
      // 获取对应优先级的颜色
      let color
      switch (priority) {
        case 'urgent': color = '#F56C6C'; break;
        case 'high': color = '#FAAD14'; break;
        case 'medium': color = '#5B8FF9'; break;
        case 'low': color = '#36CFC9'; break;
        default: color = '#5B8FF9'; // 默认蓝色
      }
      
      // 直接设置进度条的颜色和可见性
      progressBar.setAttribute('fill', color)
      progressBar.setAttribute('fill-opacity', '0.85')
      progressBar.setAttribute('stroke', color)
      progressBar.setAttribute('stroke-width', '0.5')
      
      // 强制显示
      progressBar.setAttribute('visibility', 'visible')
      progressBar.setAttribute('display', 'block')
      progressBar.setAttribute('opacity', '1')
    })
    
    console.log('进度条显示设置完成')
  } catch (error) {
    console.error('确保进度条显示时出错:', error)
  }
}

// 确保直接定位到今天的日期
const scrollTodayWithRetry = () => {
  // 添加延迟以确保DOM渲染完成
  setTimeout(() => {
    customizeHeader() // 首先确保添加today-highlight等标记
    injectTodayHighlightStyles() // 注入SVG样式
    
    // 再延迟一点执行滚动
    setTimeout(() => {
      scrollToToday() // 尝试滚动到今天
    }, 100)
  }, 200)
}

onMounted(() => {
  fetchTasks() // 组件挂载后获取任务数据
  
  // 监听页面可见性变化，当用户从其他标签页切换回来时，重新定位到今天
  const handleVisibilityChange = () => {
    if (document.visibilityState === 'visible' && ganttChart) {
      console.log('页面重新获得焦点，尝试重新定位到今天')
      // 延迟执行，确保DOM已更新
      setTimeout(() => {
        customizeHeader()
        scrollToToday()
      }, 300)
    }
  }
  
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 每30秒检查一次，如果跨越了午夜时间点，则刷新今日高亮
  // 这对长时间打开的页面特别重要
  let lastDate = new Date().getDate()
  const dateCheckTimer = setInterval(() => {
    const currentDate = new Date().getDate()
    
    // 如果日期变了（跨越了午夜），则刷新高亮
    if (currentDate !== lastDate) {
      console.log('检测到日期变化，更新今日高亮')
      lastDate = currentDate
      
      // 完全刷新甘特图和今日高亮
      if (ganttChart) {
        customizeHeader()
        scrollToToday()
      }
    }
  }, 30000) // 每30秒检查一次
  
  // 保存引用以便在组件卸载时移除
  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    clearInterval(dateCheckTimer) // 清除定时器
  })
})

watch([currentDate], () => {
  updateGanttTimeRange()
})

// 添加onActivated钩子，当组件被keep-alive激活时调用
// 这确保当用户从其他路由返回到甘特图时，能重新定位到今天
onActivated(() => {
  console.log('甘特图视图被激活，尝试重新定位到今天')
  if (ganttChart) {
    setTimeout(() => {
      customizeHeader()
      scrollToToday()
    }, 200)
  }
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
  align-items: center;
}

/* 任务开关样式 */
.task-switch {
  margin-left: 16px;
}

.task-switch :deep(.el-switch__label) {
  font-size: 14px;
  color: #606266;
}

.task-switch :deep(.el-switch__label.is-active) {
  color: #409EFF;
}

.task-switch :deep(.el-switch__core) {
  width: 50px !important;
}

/* 修改为系统标准色系 */
.task-switch :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #409EFF !important; /* 蓝色 - Element Plus 默认主色 */
  border-color: #409EFF !important;
}

.task-switch :deep(.el-switch:not(.is-checked) .el-switch__core) {
  background-color: #909399 !important; /* 灰色 - Element Plus 默认灰色 */
  border-color: #909399 !important;
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
/* 注意：SVG元素的颜色通过动态注入的CSS控制，确保覆盖库的默认样式 */
.gantt .bar-wrapper.priority-urgent .bar {
  fill: #F56C6C; /* 紧急 - 深红色 */
}

.gantt .bar-wrapper.priority-high .bar {
  fill: #FAAD14; /* 高 - 阿里系黄色 */
}

.gantt .bar-wrapper.priority-medium .bar {
  fill: #5B8FF9; /* 中 - AntV蓝色 */
}

.gantt .bar-wrapper.priority-low .bar {
  fill: #36CFC9; /* 低 - 绿色+青蓝 */
}

/* 进度条颜色匹配任务颜色 */
.gantt .bar-wrapper.priority-urgent .bar-progress {
  fill: #F56C6C;
  stroke: #F56C6C;
  opacity: 1 !important;
  visibility: visible !important;
}

.gantt .bar-wrapper.priority-high .bar-progress {
  fill: #FAAD14;
  stroke: #FAAD14;
  opacity: 1 !important;
  visibility: visible !important;
}

.gantt .bar-wrapper.priority-medium .bar-progress {
  fill: #5B8FF9;
  stroke: #5B8FF9;
  opacity: 1 !important;
  visibility: visible !important;
}

.gantt .bar-wrapper.priority-low .bar-progress {
  fill: #36CFC9;
  stroke: #36CFC9;
  opacity: 1 !important;
  visibility: visible !important;
}

/* 进度条增强可见性 */
.gantt .bar-wrapper .bar-progress {
  fill-opacity: 0.85;
  stroke-width: 0.5;
  opacity: 1 !important;
  visibility: visible !important;
  display: block !important;
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

/* 今日高亮相关的所有样式 - 合并为一处统一管理 */
/* 使用SVG属性fill和stroke，而非CSS的background-color和border */
/* 今日列的背景和边框 */
.gantt .tick.today rect {
  fill: rgba(24, 144, 255, 0.15) !important; /* 淡蓝色背景 */
  stroke: #1890ff !important; /* 蓝色描边 */
  stroke-width: 2px !important; /* 描边宽度 */
}

/* 今日列的文本 */
.gantt .tick.today text {
  fill: #1890ff !important; /* 蓝色文字 */
  font-weight: bold !important;
}

/* 表头样式（非SVG部分，使用普通CSS属性） */
.gantt .grid-header .grid-header-cell.today-highlight,
.gantt .grid-header .today-highlight {
  background-color: rgba(24, 144, 255, 0.15) !important; /* 淡蓝色背景 */
  border-top: 3px solid #1890ff !important; /* 顶部边框 */
}

/* 表头内的文字样式 */
.gantt .grid-header .today-highlight .weekday,
.gantt .grid-header .grid-header-cell.today-highlight .weekday,
.gantt .grid-header .today-highlight .date,
.gantt .grid-header .grid-header-cell.today-highlight .date {
  color: #1890ff !important; /* 蓝色文字 */
  font-weight: 800 !important;
}

/* Today标签 */
.gantt .date-header .today-mark {
  display: inline-block !important;
  padding: 0 6px !important;
  background-color: #1890ff !important; /* 蓝色背景 */
  color: white !important;
  border-radius: 4px !important;
  font-size: 12px !important;
  margin-left: 4px !important;
  font-weight: bold !important;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.4) !important;
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
