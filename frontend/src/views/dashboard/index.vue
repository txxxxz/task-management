<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h2>👋 Welcome back, {{ username }}</h2>
    </div>

    <!-- 任务状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6" v-for="(item, index) in taskStatus" :key="index">
        <el-card 
          shadow="hover" 
          :body-style="{ padding: '20px', cursor: 'pointer' }" 
          @click="handleStatusCardClick(item.status)"
        >
          <div class="status-card">
            <div class="status-info">
              <h3>{{ item.label }}</h3>
              <div class="status-number">{{ item.count }}</div>
            </div>
            <el-icon :size="40" :class="item.iconClass">
              <component :is="item.icon"></component>
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Daily task status statistics</span>
              <div class="week-controls">
                <el-button @click="changeWeek(-1)" :icon="ArrowLeft">Previous Week</el-button>
                <el-button @click="changeWeek(1)" :icon="ArrowRight">Next Week</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="taskTrendOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Task priority distribution</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="priorityOption" autoresize @click="handlePieChartClick" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { Clock, Document, Loading, Check, Close, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCurrentUserTaskStats, getTaskStatusStatsByDay, getTaskPriorityDistribution } from '@/api/task'

// 自定义图表选项类型，包含fullDates字段
interface TaskTrendChartOption {
  tooltip: any;
  legend: any;
  grid: any;
  xAxis: any;
  yAxis: any;
  series: any[];
  fullDates?: string[]; // 完整日期数据，用于tooltip显示
}

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const userStore = useUserStore()
const username = computed(() => userStore.userInfo?.username || '访客')
const chartTimeRange = ref('week')
const currentWeekOffset = ref(0)
const router = useRouter()

// 任务状态数据
const taskStatus = ref([
  {
    label: 'Pending',
    status: 'Pending',
    count: 0,
    icon: 'Clock',
    iconClass: 'status-pending',
    statusCode: 0
  },
  {
    label: 'In progress',
    status: 'In progress',
    count: 0,
    icon: 'Loading',
    iconClass: 'status-progress',
    statusCode: 1
  },
  {
    label: 'Today expired',
    status: 'Today expired',
    count: 0,
    icon: 'Document',
    iconClass: 'status-due',
    statusCode: 'expired'
  },
  {
    label: 'Completed',
    status: 'Completed',
    count: 0,
    icon: 'Check',
    iconClass: 'status-completed',
    statusCode: 2
  }
])

// 任务趋势图表配置
const taskTrendOption = ref<TaskTrendChartOption>({
  tooltip: {
    trigger: 'axis',
    formatter: function(params: any) {
      // 获取当前悬浮点的日期（X轴值对应的日期）
      const dayIndex = params[0].dataIndex;
      const fullDate = taskTrendOption.value.fullDates?.[dayIndex] || params[0].axisValue;
      
      // 计算任务总数
      let totalTasks = 0;
      params.forEach((param: any) => {
        totalTasks += param.value;
      });
      
      // 构建提示框内容
      let tooltipContent = `${fullDate}<br/>`;
      tooltipContent += `Total tasks: ${totalTasks}<br/>`;
      
      // 添加各状态的任务数量
      params.forEach((param: any) => {
        tooltipContent += `${param.seriesName}: ${param.value}<br/>`;
      });
      
      return tooltipContent;
    }
  },
  legend: {
    data: ['Pending', 'In progress', 'Completed']
  },
  grid: {
    left: '5%',
    right: '4%',
    bottom: '10%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [] as string[],
    name: 'Day of Week',
    nameLocation: 'middle',
    nameGap: 30
  },
  yAxis: {
    type: 'value',
    name: 'Number of Tasks',
    nameLocation: 'middle',
    nameGap: 45,
    axisLabel: {
      margin: 10
    }
  },
  series: [
    {
      name: 'Pending',
      type: 'line',
      data: [] as number[]
    },
    {
      name: 'In progress',
      type: 'line',
      data: [] as number[]
    },
    {
      name: 'Completed',
      type: 'line',
      data: [] as number[]
    }
  ],
  fullDates: []
})

// 优先级分布图表配置
type PieDataItem = {
  value: number;
  name: string;
  itemStyle: {
    color: string;
  };
};

const priorityOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'horizontal',
    bottom: 'bottom',
    left: 'center'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [] as PieDataItem[],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        formatter: '{b}: {c} ({d}%)'
      }
    }
  ]
})

// 处理状态卡片点击
const handleStatusCardClick = (status: string) => {
  const statusItem = taskStatus.value.find(item => item.status === status)
  
  if (statusItem) {
    if (status === 'Today expired') {
      // 今日到期任务特殊处理
      router.push('/list?todayExpired=true')
    } else {
      // 其他状态
      router.push({
        path: '/list',
        query: { status: statusItem.statusCode }
      })
    }
  }
}

// 处理饼图点击事件
const handlePieChartClick = (params: any) => {
  // 获取点击的优先级名称
  const priorityName = params.name
  
  // 优先级映射
  const priorityMap: Record<string, number> = {
    'Critical': 0,
    'High': 1,
    'Medium': 2,
    'Low': 3
  }
  
  // 如果是有效的优先级名称
  if (priorityName in priorityMap) {
    // 跳转到任务列表页面，并传递优先级参数
    router.push({
      path: '/list',
      query: { priority: priorityMap[priorityName] }
    })
  }
}

// 获取用户任务统计信息
const fetchUserTaskStats = async () => {
  try {
    const response = await getCurrentUserTaskStats()
    console.log('任务统计响应:', response)
    
    if (response && response.data) {
      // 处理响应数据，支持两种可能的格式
      const responseData = response.data as any
      const stats = responseData.data ? responseData.data : responseData
      
      console.log('任务统计数据:', stats)
      
      // 安全地更新任务状态的数量
      taskStatus.value[0].count = Number(stats.pending || 0)
      taskStatus.value[1].count = Number(stats.inProgress || 0)
      taskStatus.value[2].count = Number(stats.todayExpired || 0)
      taskStatus.value[3].count = Number(stats.completed || 0)
      
      console.log('更新后的任务状态:', taskStatus.value)
    } else {
      console.error('任务统计数据返回错误:', response)
      ElMessage.error('Get Task Status Stats Failed')
    }
  } catch (error) {
    console.error('获取任务统计信息失败:', error)
    ElMessage.error('Get Task Status Stats Failed')
  }
}

// 切换周
const changeWeek = async (offset: number) => {
  currentWeekOffset.value += offset
  await fetchTaskTrendData()
}

// 获取任务趋势数据
const fetchTaskTrendData = async () => {
  try {
    const response = await getTaskStatusStatsByDay(currentWeekOffset.value)
    console.log('任务趋势数据响应:', response)
    
    // 后端返回的data是一个对象，其中包含code和data字段，data是一个数组
    if (response.data && response.data.code === 1 && response.data.data) {
      const statsArray = response.data.data
      // 检查stats是否为数组
      if (Array.isArray(statsArray)) {
        // 星期顺序映射（从周一开始）
        const weekOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
        
        // 创建一个映射来存储每个星期对应的数据
        const dataMap: Record<string, {pending: number, inProgress: number, completed: number}> = {};
        
        // 处理每个数据项
        statsArray.forEach((item: any) => {
          const weekDay = item.day || '';
          
          dataMap[weekDay] = {
            pending: Number(item.pending) || 0,
            inProgress: Number(item.inProgress) || 0,
            completed: Number(item.completed) || 0
          };
        });
        
        // 计算当前周的起始日期（周一）
        const now = new Date();
        const todayDay = now.getDay(); // 0是周日，1是周一，...
        const mondayOffset = todayDay === 0 ? -6 : 1 - todayDay; // 计算到最近的周一的偏移量
        
        // 考虑weekOffset的影响
        const weekStartDate = new Date(now);
        weekStartDate.setDate(now.getDate() + mondayOffset + (currentWeekOffset.value * 7));
        
        // 根据星期顺序排序数据
        const sortedDays: string[] = [];
        const pendingData: number[] = [];
        const inProgressData: number[] = [];
        const completedData: number[] = [];
        const fullDates: string[] = [];
        
        // 按照固定的星期顺序提取数据
        weekOrder.forEach((weekDay, index) => {
          sortedDays.push(weekDay);
          
          // 计算当前日期
          const currentDate = new Date(weekStartDate);
          currentDate.setDate(weekStartDate.getDate() + index);
          
          // 格式化为YYYY-MM-DD
          const year = currentDate.getFullYear();
          const month = String(currentDate.getMonth() + 1).padStart(2, '0');
          const day = String(currentDate.getDate()).padStart(2, '0');
          const formattedDate = `${year}-${month}-${day}`;
          
          fullDates.push(formattedDate);
          
          if (dataMap[weekDay]) {
            pendingData.push(dataMap[weekDay].pending);
            inProgressData.push(dataMap[weekDay].inProgress);
            completedData.push(dataMap[weekDay].completed);
          } else {
            // 如果没有数据，设置为0
            pendingData.push(0);
            inProgressData.push(0);
            completedData.push(0);
          }
        });
        
        // 更新图表数据
        taskTrendOption.value.xAxis.data = sortedDays;
        taskTrendOption.value.series[0].data = pendingData;
        taskTrendOption.value.series[1].data = inProgressData;
        taskTrendOption.value.series[2].data = completedData;
        taskTrendOption.value.fullDates = fullDates;
      } else {
        console.error('任务趋势数据不是数组:', statsArray)
        ElMessage.error('Task Trend Data Format Error')
        
        // 设置空数据
        resetTrendChartData()
      }
    } else {
      console.error('任务趋势数据返回错误:', response.data)
      ElMessage.error('Get Task Trend Data Failed')
      
      // 设置空数据
      resetTrendChartData()
    }
  } catch (error) {
    console.error('获取任务趋势数据失败:', error)
    ElMessage.error('Get Task Trend Data Failed')
    
    // 设置空数据
    resetTrendChartData()
  }
}

// 重置趋势图表数据
const resetTrendChartData = () => {
  taskTrendOption.value.xAxis.data = []
  taskTrendOption.value.series.forEach(series => {
    series.data = []
  })
  taskTrendOption.value.fullDates = []
}

// 获取任务优先级分布数据
const fetchPriorityDistribution = async () => {
  try {
    const response = await getTaskPriorityDistribution()
    console.log('优先级分布响应:', response)
    
    if (response.data && response.data.code === 1 && response.data.data) {
      const priorityData = response.data.data
      // 生成饼图数据
      const pieChartData = [
        { 
          value: Number(priorityData.critical) || 0, 
          name: 'Critical',
          itemStyle: { color: '#F56C6C' } // 红色，表示紧急
        },
        { 
          value: Number(priorityData.high) || 0, 
          name: 'High',
          itemStyle: { color: '#E6A23C' } // 橙色，表示高优先级
        },
        { 
          value: Number(priorityData.medium) || 0, 
          name: 'Medium',
          itemStyle: { color: '#409EFF' } // 蓝色，表示中等优先级
        },
        { 
          value: Number(priorityData.low) || 0, 
          name: 'Low',
          itemStyle: { color: '#67C23A' } // 绿色，表示低优先级
        }
      ]
      
      // 更新图表数据
      priorityOption.value.series[0].data = pieChartData
    } else {
      console.error('获取优先级分布数据失败:', response.data)
      ElMessage.error('Get Priority Distribution Data Failed')
    }
  } catch (error) {
    console.error('获取优先级分布数据失败:', error)
    ElMessage.error('Get Priority Distribution Data Failed')
    // 重置图表数据
    priorityOption.value.series[0].data = []
  }
}

onMounted(async () => {
  // 获取用户信息
  try {
    await userStore.fetchUserInfo()
    
    // 获取任务统计信息
    await fetchUserTaskStats()
    // 获取任务趋势数据
    await fetchTaskTrendData()
    // 获取优先级分布数据
    await fetchPriorityDistribution()
  } catch (error: any) {
    let errorMessage = 'Failed to get user information'
    if (error.response) {
      // 服务器响应错误
      const { status, data } = error.response
      errorMessage += `\nStatus code: ${status}`
      errorMessage += `\nError message: ${data.msg || data.message || 'Unknown error'}`
    } else if (error.request) {
      // 请求发送失败
      errorMessage += '\nNetwork request failed, please check your network connection'
    } else {
      // 其他错误
      errorMessage += `\n${error.message || 'Unknown error'}`
    }
    console.error(errorMessage)
    ElMessage.error(errorMessage)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 24px;
}

.status-cards {
  margin-bottom: 24px;
}

.status-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info h3 {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-secondary);
}

.status-number {
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
}

.chart-section {
  margin-bottom: 24px;
}

.chart-container {
  height: 350px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-pending {
  color: var(--el-color-warning);
}

.status-progress {
  color: var(--el-color-primary);
}

.status-due {
  color: var(--el-color-danger);
}

.status-completed {
  color: var(--el-color-success);
}

.status-canceled {
  color: var(--el-color-info);
}

.week-controls {
  display: flex;
  gap: 10px;
}
</style> 