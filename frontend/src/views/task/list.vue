<template>
  <div class="task-list-container">
    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" class="filter-form">
        <!-- 第一行：任务编号、任务名称 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Name">
              <el-input v-model="filterForm.name" placeholder="Please enter task name" clearable prefix-icon="Search"/>
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Status">
              <el-select v-model="filterForm.status" placeholder="Please select status" clearable style="width: 100%">
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Priority">
              <el-select v-model="filterForm.priority" placeholder="Please select priority" clearable style="width: 100%">
                <el-option
                  v-for="item in priorityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>
      
        
        <!-- 第三行：成员、标签 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Members">
              <el-select
                v-model="filterForm.members"
                placeholder="Please select members"
                clearable
                multiple
                filterable
                remote
                :remote-method="handleSearchMembers"
                :loading="membersLoading"
                @visible-change="onMemberSelectOpen"
                style="width: 100%"
              >
                <template #prefix>
                  <el-icon v-if="membersLoading"><Loading /></el-icon>
                </template>
                
                <el-option
                  v-for="item in memberOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <div class="member-option">
                    <el-avatar 
                      :size="24" 
                      :src="item.avatar" 
                      class="member-avatar"
                    >
                      {{ item.label.charAt(0).toUpperCase() }}
                    </el-avatar>
                    <span class="member-name">{{ item.label }}</span>
                  </div>
                </el-option>
                <template #empty>
                  <div class="empty-members">
                    <p>Did not find members, please try other keywords</p>
                  </div>
                </template>
              </el-select>
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Tags">
              <el-select
                v-model="filterForm.tags"
                placeholder="Please select tags"
                clearable
                multiple
                filterable
                remote
                :remote-method="handleSearchTags"
                :loading="tagsLoading"
                @visible-change="onTagSelectOpen"
                style="width: 100%"
              >
                <el-option
                  v-for="item in tagOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <div class="tag-option">
                    <span 
                      class="color-dot" 
                      :style="{ backgroundColor: item.color || '#409EFF' }"
                    ></span>
                    {{ item.label }}
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>
        
        <!-- 第四行：开始时间、截止时间 -->
        <div class="filter-row">
          <div class="filter-item-half">
            <el-form-item label="Start">
              <el-date-picker
                v-model="filterForm.createDateRange"
                type="daterange"
                range-separator="to"
                start-placeholder="Start date"
                end-placeholder="End date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <div class="filter-item-half">
            <el-form-item label="Due">
              <el-date-picker
                v-model="filterForm.dueDateRange"
                type="daterange"
                range-separator="to"
                start-placeholder="Start date"
                end-placeholder="End date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 搜索按钮行 -->
        <div class="filter-buttons-row" style="display: flex; justify-content: center;">
          <div class="filter-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> Search
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon> Reset
            </el-button>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <div class="left-buttons">
        <el-button type="primary" @click="handleNewTask" v-if="isLeader">
          <el-icon><Plus /></el-icon> New Task
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
      :header-cell-style="{background:'#f5f7fa', color:'#606266', fontWeight: '600', height: '44px'}"
      :cell-style="{padding: '4px'}"
      :row-style="{height: '46px'}"
    >
      <template #empty>
        <div class="empty-table">
          <el-empty description="No task data" :image-size="100">
            <template #image>
              <el-icon :size="80" style="opacity: 1;">
                <Document />
              </el-icon>
            </template>
            <template #description>
              <p>No task data</p>
              <p class="empty-tip">
                <template v-if="isLeader">
                  You can click the button below to create a new task
                </template>
                <template v-else>
                  No tasks found in the current view
                </template>
              </p>
            </template>
            <el-button type="primary" @click="handleNewTask" v-if="isLeader">Create Task</el-button>
          </el-empty>
        </div>
      </template>
      <el-table-column label="No." width="60" align="center">
        <template #default="scope">
          <span class="index-cell">{{ getRowIndex(scope.$index) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="Task Name" min-width="160" align="left">
        <template #default="{ row }">
          <el-link 
            type="primary" 
            :underline="false"
            @click="handleViewDetail(row)"
            class="task-name-link"
          >
            {{ row.name || row.title || 'Unnamed Task' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="projectName" label="Project Name" min-width="120" align="center">
        <template #default="{ row }">
          <el-tooltip
            :content="formatProjectName(row.projectName)"
            placement="top"
            :disabled="!row.projectName || row.projectName.length < 8"
          >
            <el-tag
              type="info"
              effect="plain"
              size="small"
              class="project-tag"
            >
              {{ formatProjectName(row.projectName) }}
            </el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="Priority" width="100" align="center">
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
      <el-table-column prop="status" label="Status" width="100" align="center">
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
      <el-table-column prop="startTime" label="Start Time" width="160">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="Due Time" width="160">
        <template #default="{ row }">
          {{ formatDate(row.deadline || row.dueTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Operation" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-buttons">
            <!-- 针对leader显示编辑和删除按钮 -->
            <template v-if="isLeader">
              <el-tooltip content="Edit Task" placement="top">
                <el-button
                  type="primary"
                  link
                  @click="handleEditTask(row)"
                  class="action-button"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="Delete Task" placement="top">
                <el-button
                  type="danger"
                  link
                  @click="handleDeleteTask(row)"
                  class="action-button"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
            
            <!-- 针对普通成员只显示查看按钮 -->
            <template v-else>
              <el-tooltip content="View Task" placement="top">
                <el-button
                  type="info"
                  link
                  @click="handleViewDetail(row)"
                  class="action-button"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
          </div>
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
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { Search, Refresh, Plus, Download, View, Edit, Delete, Loading, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTaskList, deleteTask } from '@/api/task'
import { getAllUsers } from '@/api/user'
import { getTagList, getTagsByProjectId, searchTags } from '@/api/tag'
import { formatDate as formatDateUtil } from '@/utils/format'
import type { TaskDetail, TaskQueryParams } from '@/types/task'
import { getProjectDetail } from '@/api/project'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断当前用户是否为项目负责人
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

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
  { label: 'Critical', value: 4 },
  { label: 'High', value: 3 },
  { label: 'Medium', value: 2 },
  { label: 'Low', value: 1 }
]

const statusOptions = [
  { label: 'Pending', value: 0 },
  { label: 'In Progress', value: 1 },
  { label: 'Completed', value: 2 },
  { label: 'Cancelled', value: 3 }
]

// 从API获取这些选项，这里先使用静态数据
interface MemberOption {
  label: string;
  value: string;
  avatar: string;
}

interface TagOption {
  label: string;
  value: string;
  color?: string;
}

const memberOptions = ref<MemberOption[]>([
  { label: 'Tom', value: 'member1', avatar: '' },
  { label: 'Sarah', value: 'member2', avatar: '' },
  { label: 'David', value: 'member3', avatar: '' }
])

const tagOptions = ref<TagOption[]>([
  { label: 'Bug', value: 'bug', color: '#f56c6c' },
  { label: 'Feature', value: 'feature', color: '#409EFF' },
  { label: 'Optimization', value: 'optimization', color: '#67c23a' }
])

// API加载状态
const membersLoading = ref(false)
const tagsLoading = ref(false)

// 搜索关键词
const memberSearchKeyword = ref('')
const tagSearchKeyword = ref('')

// 搜索防抖定时器
const memberSearchTimer = ref<number | null>(null)
const tagSearchTimer = ref<number | null>(null)

// 表格数据
const loading = ref(false)
const taskList = ref<TaskDetail[]>([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 使用loadingCount统计正在加载的部分，当所有部分都加载完成后才清除loading状态
const loadingCount = ref(0)

// 监听路由参数变化并自动筛选
onMounted(() => {
  // 从URL参数中获取筛选条件
  const { status, priority, keyword, todayExpired } = route.query
  
  if (status) {
    filterForm.status = Number(status)
  }
  
  if (priority) {
    filterForm.priority = Number(priority)
  }
  
  if (keyword) {
    filterForm.name = keyword as string
  }
  
  // 处理今日到期参数
  if (todayExpired === 'true') {
    // 设置日期范围为今天
    const today = new Date().toISOString().split('T')[0] // 格式: YYYY-MM-DD
    filterForm.dueDateRange = [today, today]
  }
  
  // 加载任务列表
  fetchTaskList()
  // 加载用户和标签列表
  fetchMemberOptions()
  fetchTagOptions()
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

// 格式化日期为YYYY-MM-DD格式
const formatDateForApi = (dateStr: string): string => {
  try {
    // 确保日期格式为YYYY-MM-DD
    const date = new Date(dateStr);
    return date.toISOString().split('T')[0];
  } catch (e) {
    return dateStr; // 出错则返回原字符串
  }
}

// get task list from backend
const fetchTaskList = async () => {
  loadingCount.value++
  loading.value = true
  try {
    // 构建查询参数对象
    const cleanParams: TaskQueryParams = {}
    
    // 基本查询参数
    if (filterForm.name) cleanParams.keyword = filterForm.name
    if (typeof filterForm.status === 'number') cleanParams.status = filterForm.status
    if (typeof filterForm.priority === 'number') cleanParams.priority = filterForm.priority
    
    // 成员筛选
    if (filterForm.members && filterForm.members.length > 0) {
      cleanParams.member = filterForm.members[0] // 使用第一个成员作为搜索条件
      console.log(`Search tasks by member[${filterForm.members[0]}]`)
    }
    
    // 标签筛选
    if (filterForm.tags && filterForm.tags.length > 0) {
      cleanParams.tags = filterForm.tags.join(',')
    }
    
    // 创建时间范围处理
    if (filterForm.createDateRange && filterForm.createDateRange.length === 2) {
      cleanParams.startTime = formatDateForApi(filterForm.createDateRange[0])
      cleanParams.endTime = formatDateForApi(filterForm.createDateRange[1])
      console.log(`使用创建时间范围: ${cleanParams.startTime} 至 ${cleanParams.endTime}`)
    }
    
    // 截止时间范围处理
    if (filterForm.dueDateRange && filterForm.dueDateRange.length === 2) {
      cleanParams.dueStartTime = formatDateForApi(filterForm.dueDateRange[0])
      cleanParams.dueEndTime = formatDateForApi(filterForm.dueDateRange[1])
      console.log(`使用截止时间范围: ${cleanParams.dueStartTime} 至 ${cleanParams.dueEndTime}`)
    }
    
    // 特殊处理今日到期参数
    if (route.query.todayExpired === 'true') {
      const today = new Date().toISOString().split('T')[0]; // 格式: YYYY-MM-DD
      cleanParams.dueStartTime = today
      cleanParams.dueEndTime = today
      console.log(`使用今日到期条件: ${today}`)
      
      // 同步更新表单状态
      filterForm.dueDateRange = [today, today]
    }
    
    // 项目ID处理
    if (route.query.projectId) {
      cleanParams.projectId = route.query.projectId as string
    }
    
    // 分页参数
    cleanParams.page = currentPage.value
    cleanParams.pageSize = pageSize.value
    
    // 打印最终请求参数
    console.log('最终请求参数:', JSON.stringify(cleanParams))
    
    // 发送请求
    const response = await getTaskList(cleanParams)
    console.log('API响应状态:', response.status)
    console.log('API响应头部:', response.headers)
    console.log('API响应数据:', JSON.stringify(response.data))
    
    let tasksData: TaskDetail[] = [];
    let totalCount = 0;
    
    // 提取任务数据 - 根据后端的实际响应格式进行调整
    if (response && response.data) {
      if (typeof response.data === 'object') {
        // 判断响应格式
        if ('code' in response.data && response.data.code === 1) {
          // 标准响应格式 {code: 1, data: {...}, message: ...}
          const apiData = response.data.data;
          
          if (apiData && typeof apiData === 'object') {
            if ('items' in apiData && Array.isArray(apiData.items)) {
              tasksData = apiData.items;
              totalCount = apiData.total || apiData.items.length;
            } else if (Array.isArray(apiData)) {
              tasksData = apiData;
              totalCount = apiData.length;
            }
          }
        } else if ('items' in response.data && Array.isArray(response.data.items)) {
          // 直接返回分页对象 {items: [...], total: ...}
          tasksData = response.data.items;
          totalCount = response.data.total || response.data.items.length;
        } else if (Array.isArray(response.data)) {
          // 直接返回数组
          tasksData = response.data;
          totalCount = response.data.length;
        }
      }
    }
    
    console.log('提取的任务数据:', JSON.stringify(tasksData));
    console.log('总数:', totalCount);
    
    // 处理任务数据，确保关键字段存在并格式化
    taskList.value = processTaskData(tasksData);
    total.value = totalCount;
    
    console.log(`成功加载 ${taskList.value.length} 条任务记录, 总计 ${total.value} 条`);
    
    // 检查是否缺少项目名称，并记录信息
    const missingProjectNames = taskList.value.filter(task => 
      !task.projectName || task.projectName === '未分配'
    );
    
    if (missingProjectNames.length > 0) {
      console.warn(`有 ${missingProjectNames.length} 条任务缺少项目名称:`, 
        missingProjectNames.map(t => ({ id: t.id, projectId: t.projectId }))
      );
      
      // 尝试通过项目ID查询项目名称
      missingProjectNames.forEach(task => {
        if (task.projectId) {
          fetchProjectName(task.projectId).then(projectName => {
            if (projectName) {
              task.projectName = projectName;
              console.log(`已为任务 ${task.id} 补充项目名称(API): ${projectName}`);
              
              // 强制更新视图
              taskList.value = [...taskList.value];
            }
          }).catch(err => {
            console.error(`获取项目详情失败，项目ID: ${task.projectId}`, err);
          });
        }
      });
    }
  } catch (error: any) {
    console.error('获取任务列表失败:', error)
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误响应:', error.response.data)
    }
    ElMessage.error(`Get task list failed: ${error.message || 'Please try again later'}`)
    // 清空数据
    taskList.value = []
    total.value = 0
  } finally {
    loadingCount.value--
    if (loadingCount.value <= 0) {
      loading.value = false
      loadingCount.value = 0 // 确保不会变成负数
    }
  }
}

// 处理任务数据，确保所有必要字段存在
const processTaskData = (tasks: any[]): TaskDetail[] => {
  console.log('原始任务数据:', JSON.stringify(tasks));
  
  // 记录每个任务的projectId和projectName
  tasks.forEach((task, index) => {
    console.log(`任务 #${index + 1} ID=${task.id}:`, {
      projectId: task.projectId,
      projectName: task.projectName,
      // 检查原始数据里是否有项目名称字段
      hasProjectName: 'projectName' in task,
      project_name: task.project_name
    });
  });
  
  return tasks.map(task => {
    // 尝试从可能的字段中提取项目名称
    let projectName = null;
    
    // 标准驼峰命名
    if (task.projectName !== undefined && task.projectName !== null) {
      projectName = task.projectName;
    }
    // 下划线命名风格（后端SQL常用）
    else if (task.project_name !== undefined && task.project_name !== null) {
      projectName = task.project_name;
    }
    // 可能嵌套在project对象中
    else if (task.project && typeof task.project === 'object') {
      if (task.project.name) {
        projectName = task.project.name;
      }
    }
    
    // 确保基本字段存在
    const processedTask: TaskDetail = {
      id: task.id?.toString() || `temp-${Date.now()}`,
      name: task.name || task.title || '未命名任务',
      description: task.description || '',
      createTime: task.createTime || task.createAt || new Date().toISOString(),
      priority: typeof task.priority === 'number' ? task.priority : 2,
      status: typeof task.status === 'number' ? task.status : 0,
      members: Array.isArray(task.members) ? task.members : [],
      tags: Array.isArray(task.tags) ? task.tags : [],
      projectId: task.projectId?.toString() || '',
      // 使用前面提取的项目名称，如果没有则默认为未分配
      projectName: projectName || '未分配'
    };
    
    // 处理截止时间字段
    if (task.deadline) {
      processedTask.deadline = task.deadline;
    } else if (task.dueTime) {
      processedTask.dueTime = task.dueTime;
    }
    
    // 处理开始时间 - 重要
    if (task.startTime) {
      processedTask.startTime = task.startTime;
    }
    
    // 处理附件
    if (Array.isArray(task.attachments)) {
      processedTask.attachments = task.attachments;
    }
    
    // 在全局缓存项目ID和名称的映射，以便后续使用
    if (processedTask.projectId && processedTask.projectName && processedTask.projectName !== '未分配') {
      if (!window.taskProjectMapping) {
        window.taskProjectMapping = {};
      }
      window.taskProjectMapping[processedTask.projectId] = processedTask.projectName;
    }
    
    return processedTask;
  });
};

// 获取用户列表
const fetchMemberOptions = async () => {
  membersLoading.value = true
  try {
    const params = {
      role: '0,1', // 只查询角色为0和1的用户
      page: 1,
      pageSize: 50
    }
    const response = await getAllUsers(params)
    
    if (response && response.data && response.data.code === 1 && response.data.data) {
      const { items } = response.data.data
      if (Array.isArray(items)) {
        memberOptions.value = items.map(user => ({
          value: user.username,
          label: `${user.username} (${user.role === 0 ? 'Member' : 'Leader'})`,
          avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }))
      } else {
        console.warn('返回的items不是数组:', items)
      }
    } else {
      console.warn('API响应格式不符合预期:', response)
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    // 保持默认选项，不显示错误信息以免影响用户体验
  } finally {
    membersLoading.value = false
  }
}

// 获取标签列表
const fetchTagOptions = async () => {
  tagsLoading.value = true
  try {
    // 如果有项目ID，则按项目获取标签
    const projectId = route.query.projectId ? Number(route.query.projectId) : undefined
    
    if (projectId) {
      const response = await getTagsByProjectId(projectId)
      if (response?.data?.code === 1 && response.data.data) {
        const tagsData = response.data.data;
        if (tagsData.items && Array.isArray(tagsData.items)) {
          tagOptions.value = tagsData.items.map(tag => ({
            label: tag.name,
            value: tag.id,
            color: tag.color
          }))
        }
      }
    } else {
      // 否则获取所有标签
      const response = await getTagList()
      if (response?.data?.code === 1 && response.data.data) {
        const tagsData = response.data.data;
        if (tagsData.items && Array.isArray(tagsData.items)) {
          tagOptions.value = tagsData.items.map(tag => ({
            label: tag.name,
            value: tag.id,
            color: tag.color
          }))
        }
      }
    }
  } catch (error) {
    console.error('获取标签列表失败', error)
    ElMessage.warning('Failed to load tag list')
  } finally {
    tagsLoading.value = false
  }
}

// 搜索用户 - 添加防抖
const handleSearchMembers = (query: string) => {
  // 清除现有定时器
  if (memberSearchTimer.value) {
    clearTimeout(memberSearchTimer.value)
    memberSearchTimer.value = null
  }
  
  // 设置新的定时器，延迟300ms执行搜索
  memberSearchTimer.value = window.setTimeout(() => {
    doSearchMembers(query)
  }, 300)
}

// 实际执行搜索
const doSearchMembers = async (query: string) => {
  if (query) {
    memberSearchKeyword.value = query
    membersLoading.value = true
    try {
      const params = {
        keyword: query,
        role: '0,1', // 只查询角色为0和1的用户
        page: 1,
        pageSize: 20
      }
      const response = await getAllUsers(params)
      
      if (response && response.data && response.data.code === 1 && response.data.data) {
        const { items } = response.data.data
        if (Array.isArray(items)) {
          memberOptions.value = items.map(user => ({
            value: user.username,
            label: `${user.username} (${user.role === 0 ? 'Member' : 'Leader'})`,
            avatar: user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
          }))
        } else {
          console.warn('返回的items不是数组:', items)
        }
      } else {
        console.warn('API响应格式不符合预期:', response)
      }
    } catch (error) {
      console.error('搜索用户失败', error)
      // 不显示错误信息，保持原有数据
    } finally {
      membersLoading.value = false
    }
  } else {
    // 如果查询为空，重新获取默认列表
    fetchMemberOptions()
  }
}

// 搜索标签 - 添加防抖
const handleSearchTags = (query: string) => {
  // 清除现有定时器
  if (tagSearchTimer.value) {
    clearTimeout(tagSearchTimer.value)
    tagSearchTimer.value = null
  }
  
  // 设置新的定时器，延迟300ms执行搜索
  tagSearchTimer.value = window.setTimeout(() => {
    doSearchTags(query)
  }, 300)
}

// 实际执行搜索
const doSearchTags = async (query: string) => {
  if (query) {
    tagSearchKeyword.value = query
    tagsLoading.value = true
    try {
      const projectId = route.query.projectId ? Number(route.query.projectId) : undefined
      const response = await searchTags(query, projectId)
      
      if (response?.data?.code === 1 && response.data.data) {
        const tagsData = response.data.data;
        if (tagsData.items && Array.isArray(tagsData.items)) {
          tagOptions.value = tagsData.items.map(tag => ({
            label: tag.name,
            value: tag.id,
            color: tag.color
          }))
        }
      }
    } catch (error) {
      console.error('搜索标签失败', error)
    } finally {
      tagsLoading.value = false
    }
  } else {
    // 如果查询为空，重新获取默认列表
    fetchTagOptions()
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
  switch (priority) {
    case 4: return 'Critical'
    case 3: return 'High'
    case 2: return 'Medium'
    case 1: return 'Low'
    default: return 'Unknown'
  }
}

const getStatusType = (status: number): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  const types: Record<number, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
    0: 'primary',       // 待处理
    1: 'warning',    // 进行中
    2: 'success',    // 已完成
    3: 'info'      // 已取消
  }
  return types[status] || 'primary'
}

const getStatusText = (status: number): string => {
  switch (status) {
    case 0: return 'Pending'
    case 1: return 'In Progress'
    case 2: return 'Completed'
    case 3: return 'Cancelled'
    default: return 'Unknown'
  }
}

const formatDate = (date: string | undefined): string => {
  if (!date) return '未设置'
  
  try {
    // 尝试将日期格式化为本地时间
    const dateObj = new Date(date)
    if (isNaN(dateObj.getTime())) {
      return '日期无效'
    }
    
    // 格式化为 YYYY-MM-DD HH:mm 格式
    const year = dateObj.getFullYear()
    const month = String(dateObj.getMonth() + 1).padStart(2, '0')
    const day = String(dateObj.getDate()).padStart(2, '0')
    const hours = String(dateObj.getHours()).padStart(2, '0')
    const minutes = String(dateObj.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (e) {
    console.error('日期格式化错误:', e)
    return date
  }
}

// 事件处理方法
const handleSearch = () => {
  // 重置为第一页
  currentPage.value = 1 
  
  // 记录本次搜索条件
  console.log('执行搜索，搜索条件:', {
    keyword: filterForm.name,
    status: filterForm.status,
    priority: filterForm.priority,
    members: filterForm.members,
    tags: filterForm.tags,
    createDateRange: filterForm.createDateRange,
    dueDateRange: filterForm.dueDateRange
  });
  
  // 获取任务列表
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
  router.push({ query: { projectId: route.query.projectId } })
  
  // 重新加载数据
  currentPage.value = 1
  fetchTaskList()
  
  // 重新加载用户和标签选项
  fetchMemberOptions()
  fetchTagOptions()
}

const handleViewDetail = (row: TaskDetail) => {
  router.push(`/detail/${row.id}`)
}

const handleNewTask = () => {
  router.push('/create')
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTaskList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTaskList()
}

const handleEditTask = (row: TaskDetail) => {
  if (!isLeader.value) {
    ElMessage.warning('Only project leaders can edit tasks')
    return
  }
  
  // 跳转到编辑页面
  router.push(`/detail/${row.id}`)
}

const handleDeleteTask = (row: TaskDetail) => {
  if (!isLeader.value) {
    ElMessage.warning('Only project leaders can delete tasks')
    return
  }

  ElMessageBox.confirm(
    `Are you sure you want to delete the task "${row.name || row.title || 'Unnamed Task'}"? This action cannot be undone!`,
    'Delete Confirmation',
    {
      confirmButtonText: 'Confirm Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  )
    .then(async () => {
      try {
        await deleteTask(row.id)
        ElMessage.success('Delete Success')
        // 刷新任务列表
        fetchTaskList()
      } catch (error) {
        console.error('删除任务失败:', error)
        ElMessage.error('Delete Task Failed, Please try again later')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 处理成员选择框打开事件
const onMemberSelectOpen = (opened: boolean) => {
  if (opened) {
    // 如果列表为空或者只有默认值，则加载数据
    if (memberOptions.value.length <= 3) {
      fetchMemberOptions()
    }
  }
}

// 处理标签选择框打开事件
const onTagSelectOpen = (opened: boolean) => {
  if (opened) {
    // 如果列表为空或者只有默认值，则加载数据
    if (tagOptions.value.length <= 3) {
      fetchTagOptions()
    }
  }
}

// 添加格式化工具函数，确保数据类型安全
function safelyGetTaskData(data: any): TaskDetail[] {
  // 如果数据是null或undefined，返回空数组
  if (data == null) {
    return [];
  }
  
  // 尝试从各种可能的格式中提取任务列表
  if (Array.isArray(data)) {
    // 直接是数组
    return ensureTaskFields(data);
  } else if (typeof data === 'object') {
    // 可能是嵌套对象
    if (data.items && Array.isArray(data.items)) {
      // 标准分页格式
      return ensureTaskFields(data.items);
    } else if (data.data) {
      // API包装格式
      if (Array.isArray(data.data)) {
        return ensureTaskFields(data.data);
      } else if (data.data.items && Array.isArray(data.data.items)) {
        return ensureTaskFields(data.data.items);
      }
    } else if (data.list && Array.isArray(data.list)) {
      // 另一种常见命名
      return ensureTaskFields(data.list);
    } else if (data.tasks && Array.isArray(data.tasks)) {
      // 任务特定命名
      return ensureTaskFields(data.tasks);
    }
  }
  
  // 无法识别的格式，返回空数组
  console.warn('无法从API响应中提取任务数据:', data);
  return [];
}

// 序号生成函数
const getRowIndex = (index: number): number => {
  // 将currentPage和pageSize转换为数字类型
  const currentPageNum = Number(currentPage.value);
  const pageSizeNum = Number(pageSize.value);
  return (currentPageNum - 1) * pageSizeNum + index + 1;
};

// 确保任务对象包含所需的所有字段
function ensureTaskFields(tasks: any[]): TaskDetail[] {
  return tasks.map(task => {
    const safeTask: TaskDetail = {
      id: task.id?.toString() || `temp-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
      name: task.name || task.title || '未命名任务',
      description: task.description || '',
      createTime: task.createTime || task.createAt || task.createdAt || new Date().toISOString(),
      priority: typeof task.priority === 'number' ? task.priority : 2,
      status: typeof task.status === 'number' ? task.status : 0,
      members: Array.isArray(task.members) ? task.members : [],
      tags: Array.isArray(task.tags) ? task.tags : [],
      projectId: task.projectId?.toString() || '',
      projectName: task.projectName || '',
    };
    
    // 处理截止时间字段
    if (task.deadline) {
      safeTask.deadline = task.deadline;
    } else if (task.dueTime) {
      safeTask.dueTime = task.dueTime;
    }
    
    // 处理开始时间
    if (task.startTime) {
      safeTask.startTime = task.startTime;
    }
    
    // 处理附件
    if (Array.isArray(task.attachments)) {
      safeTask.attachments = task.attachments;
    }
    
    return safeTask;
  });
}

const formatProjectName = (name?: string): string => {
  if (!name || name === 'undefined' || name === 'null') {
    return '未分配';
  }
  return name;
};

// 获取项目名称的辅助函数
async function fetchProjectName(projectId: string): Promise<string | null> {
  if (!projectId) return null;
  
  // 首先检查缓存
  if (window.taskProjectMapping?.[projectId]) {
    return window.taskProjectMapping[projectId];
  }
  
  try {
    // 从API获取项目详情
    const response = await getProjectDetail(Number(projectId));
    console.log('项目详情API响应:', JSON.stringify(response.data));
    
    // 处理不同可能的响应格式
    let projectName = null;
    if (response?.data?.code === 1 && response.data.data) {
      projectName = response.data.data.name;
    } else if (response?.data?.name) {
      projectName = response.data.name;
    }
    
    // 如果成功获取项目名称
    if (projectName) {
      // 更新缓存
      if (!window.taskProjectMapping) {
        window.taskProjectMapping = {};
      }
      window.taskProjectMapping[projectId] = projectName;
      
      return projectName;
    }
    
    return null;
  } catch (error) {
    console.error(`获取项目详情失败，项目ID: ${projectId}`, error);
    return null;
  }
}
</script>

<style scoped>
/* 整体容器 */
.task-list-container {
  padding: 20px 40px;
  background-color: #f5f7fa;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 搜索筛选区域 */
.filter-section {
  background-color: #fff;
  padding: 30px 30px;
  border-radius: 10px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.filter-row {
  display: flex;
  gap: 30px;
  margin-bottom: 12px;
}

.filter-item-half {
  flex: 1;
  min-width: 0;
  display: flex;
  gap: 30px;
}

.filter-item-half .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

/* 确保右列的表单项左对齐 */
.filter-item-half:last-child {
  justify-content: flex-start;
}

.filter-buttons-row {
  display: flex;
  justify-content: flex-start;
  margin-top: 20px;
  padding-left: 75px;
}

.filter-buttons {
  display: flex;
  gap: 15px;
}

.filter-buttons .el-button {
  padding: 8px 20px;
  font-weight: 500;
  min-width: 80px;
}

/* 操作按钮区域 */
.operation-section {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表格区域 */
:deep(.el-table) {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  padding: 8px 0;
}

:deep(.el-table td) {
  padding: 8px;
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 0;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  padding-right: 12px;
  width: 75px !important;
}

:deep(.el-form-item__content) {
  flex: 1;
  margin-left: 0 !important;
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

/* 分页区域 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 标签选项中的颜色点 */
.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

/* 标签选项样式 */
.tag-option {
  display: flex;
  align-items: center;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .task-list-container {
    padding: 10px;
  }
  
  .filter-section {
    padding: 15px;
  }
  
  .filter-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-item-half {
    width: 100%;
    gap: 12px;
  }
  
  .filter-buttons-row {
    padding-left: 0;
    justify-content: center;
  }
}
</style> 