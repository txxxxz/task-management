<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h2>👋 欢迎回来，{{ username }}</h2>
    </div>

    <!-- 任务状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6" v-for="(item, index) in taskStatus" :key="index">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
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
              <span>每日任务状态统计</span>
              <el-radio-group v-model="chartTimeRange" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
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
              <span>任务优先级分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="priorityOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card class="task-list">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" size="small">查看更多</el-button>
        </div>
      </template>
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="deadline" label="截止日期" width="180" />
        <el-table-column prop="priority" label="优先级" width="120">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
import { Clock, Document, Loading, Check } from '@element-plus/icons-vue'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const username = ref('GuineaPig')
const chartTimeRange = ref('week')

// 任务状态数据
const taskStatus = ref([
  {
    label: '待处理',
    count: 5,
    icon: 'Clock',
    iconClass: 'status-pending'
  },
  {
    label: '进行中',
    count: 6,
    icon: 'Loading',
    iconClass: 'status-progress'
  },
  {
    label: '今日到期',
    count: 1,
    icon: 'Document',
    iconClass: 'status-due'
  },
  {
    label: '已完成',
    count: 1,
    icon: 'Check',
    iconClass: 'status-completed'
  }
])

// 任务趋势图表配置
const taskTrendOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['待处理', '进行中', '已完成']
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
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '待处理',
      type: 'line',
      data: [5, 6, 4, 8, 7, 5, 4]
    },
    {
      name: '进行中',
      type: 'line',
      data: [3, 4, 6, 4, 5, 3, 2]
    },
    {
      name: '已完成',
      type: 'line',
      data: [2, 3, 1, 4, 3, 2, 1]
    }
  ]
})

// 优先级分布图表配置
const priorityOption = ref({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [
        { value: 2, name: '紧急' },
        { value: 4, name: '高' },
        { value: 6, name: '中' },
        { value: 3, name: '低' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 任务列表数据
const taskList = ref([
  {
    id: 1,
    title: '实现响应式布局',
    deadline: '2024-12-12',
    priority: '高',
    status: '进行中'
  },
  {
    id: 2,
    title: '修复登录页面认证问题',
    deadline: '2024-12-25',
    priority: '中',
    status: '待处理'
  },
  {
    id: 3,
    title: '进行跨浏览器测试',
    deadline: '2024-12-09',
    priority: '低',
    status: '待处理'
  },
  {
    id: 4,
    title: '编写单元测试用例',
    deadline: '2024-12-08',
    priority: '紧急',
    status: '进行中'
  },
  {
    id: 5,
    title: '设置API集成测试',
    deadline: '2025-01-04',
    priority: '中',
    status: '待处理'
  }
])

// 获取优先级标签类型
const getPriorityType = (priority: string): 'success' | 'warning' | 'info' | 'danger' | '' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger' | ''> = {
    '紧急': 'danger',
    '高': 'warning',
    '中': '',
    '低': 'info'
  }
  return types[priority] || ''
}

// 获取状态标签类型
const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' | '' => {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger' | ''> = {
    '待处理': 'info',
    '进行中': 'warning',
    '已完成': 'success'
  }
  return types[status] || ''
}

onMounted(() => {
  // 在这里可以调用API获取实际数据
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
</style> 