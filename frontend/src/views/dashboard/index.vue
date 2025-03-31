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
              <el-radio-group v-model="chartTimeRange" size="small">
                <el-radio-button label="week">Week</el-radio-button>
                <el-radio-button label="month">Month</el-radio-button>
              </el-radio-group>
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
            <v-chart :option="priorityOption" autoresize />
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
import { Clock, Document, Loading, Check, Close } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCurrentUserTaskStats } from '@/api/task'

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
const router = useRouter()

// 任务状态数据
const taskStatus = ref([
  {
    label: '待处理',
    status: 'Pending',
    count: 0,
    icon: 'Clock',
    iconClass: 'status-pending',
    statusCode: 0
  },
  {
    label: '进行中',
    status: 'In progress',
    count: 0,
    icon: 'Loading',
    iconClass: 'status-progress',
    statusCode: 1
  },
  {
    label: '今日到期',
    status: 'Today expired',
    count: 0,
    icon: 'Document',
    iconClass: 'status-due',
    statusCode: 'expired'
  },
  {
    label: '已完成',
    status: 'Completed',
    count: 0,
    icon: 'Check',
    iconClass: 'status-completed',
    statusCode: 2
  }
])

// 任务趋势图表配置
const taskTrendOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['Pending', 'In progress', 'Completed']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: 'Pending',
      type: 'line',
      data: [5, 6, 4, 8, 7, 5, 4]
    },
    {
      name: 'In progress',
      type: 'line',
      data: [3, 4, 6, 4, 5, 3, 2]
    },
    {
      name: 'Completed',
      type: 'line',
      data: [2, 3, 1, 4, 3, 2, 1]
    }
  ]
})

// 优先级分布图表配置
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
      data: [
        { 
          value: 2, 
          name: 'Critical',
          itemStyle: {
            color: '#F56C6C' // 红色，表示紧急
          }
        },
        { 
          value: 4, 
          name: 'High',
          itemStyle: {
            color: '#E6A23C' // 橙色，表示高优先级
          }
        },
        { 
          value: 6, 
          name: 'Medium',
          itemStyle: {
            color: '#409EFF' // 蓝色，表示中等优先级
          }
        },
        { 
          value: 3, 
          name: 'Low',
          itemStyle: {
            color: '#67C23A' // 绿色，表示低优先级
          }
        }
      ],
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

// 获取用户任务统计信息
const fetchUserTaskStats = async () => {
  try {
    const response = await getCurrentUserTaskStats()
    console.log('任务统计响应:', response)
    
    if (response.data && response.data.data) {
      const stats = response.data.data
      console.log('任务统计数据:', stats)
      
      // 直接更新任务状态的数量
      taskStatus.value[0].count = stats.pending
      taskStatus.value[1].count = stats.inProgress
      taskStatus.value[2].count = stats.todayExpired
      taskStatus.value[3].count = stats.completed
      
      console.log('更新后的任务状态:', taskStatus.value)
    }
  } catch (error) {
    console.error('获取任务统计信息失败:', error)
    ElMessage.error('获取任务统计信息失败')
  }
}

onMounted(async () => {
  // 获取用户信息
  try {
    await userStore.fetchUserInfo()
    
    // 获取任务统计信息
    await fetchUserTaskStats()
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
  height: 300px;
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
</style> 