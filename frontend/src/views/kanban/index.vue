<template>
  <div class="kanban-container">
    <!-- 顶部搜索和标签页 -->
    <div class="kanban-header">
      <div class="tabs-container">
        <el-tabs v-model="activeTab" class="kanban-tabs">
          <el-tab-pane label="All" name="all" />
          <el-tab-pane label="Pending" name="pending" />
          <el-tab-pane label="In progress" name="in-process" />
          <el-tab-pane label="Completed" name="completed" />
        </el-tabs>
        <!-- Admin全局视图开关 -->
        <div v-if="isAdmin" class="admin-switch">
          <el-switch
            v-model="showAllTasks"
            active-text="All Tasks"
            inactive-text="My Tasks"
            inline-prompt
            @change="fetchTasks"
          />
        </div>
      </div>
      <div class="header-right" >
        <el-select
          v-model="selectedProject"
          placeholder="Select project"
          clearable
          class="project-select"
          @change="handleProjectChange"
        >
          <el-option
            v-for="project in projectList"
            :key="project.id"
            :label="project.name"
            :value="String(project.id)"
          />
        </el-select>
        <el-button v-if="isLeader" type="primary" @click="handleCreateTask">
          <el-icon><Plus /></el-icon> Create task
        </el-button>
      </div>
    </div>

    <!-- 任务看板 -->
    <div class="kanban-board">
      <!-- 待处理任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'pending'" class="task-section">
        <div class="section-header">
          <h2>Pending</h2>
          <span class="task-count">{{ pendingTasks.length }}</span>
        </div>
        <div class="task-row">
          <!-- 新建任务卡片 -->
          <el-card v-if="isLeader" class="task-card add-task-card" @click="handleNavigateToCreate">
            <div class="add-task-content">
              <el-icon :size="32" style="color: #909399;"><Plus /></el-icon>
              <p style="margin-top: 12px; color: #909399;">Click to create a new task</p>
            </div>
          </el-card>
          <!-- 待处理任务列表 -->
          <el-card 
            v-for="task in pendingTasks" 
            :key="task.id" 
            class="task-card" 
            :data-priority="getPriorityName(task.priority)"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title || task.name }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ getPriorityName(task.priority) }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName || 'Unknown Project' }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="card-content">
              <p>Create time: {{ task.createTime }}</p>
              <p>Start time: {{ task.startTime }}</p>
              <p>
                <span>Due time: </span>
                <span :class="{ 'expired-date': isTaskExpired(task) }">{{ task.deadline || task.dueTime }}</span>
              </p>
              <div class="tags-container">
                <el-tag v-for="tag in task.tags" :key="tag" size="small" class="mx-1">
                  {{ tag }}
                </el-tag>
              </div>
              <div class="task-members">
                <el-tooltip
                  v-for="member in task.members"
                  :key="member"
                  :content="member"
                  placement="top"
                  effect="light"
                  :show-after="200"
                >
                  <el-avatar
                    :size="28"
                    class="member-avatar"
                    @click.stop="isLeader && handleRemoveMember(task, member)"
                    :title="isLeader ? 'Click to remove member' : member"
                  >
                    {{ member.charAt(0) }}
                  </el-avatar>
                </el-tooltip>
                <el-button
                  v-if="isLeader"
                  circle
                  size="small"
                  @click.stop="handleAddMember(task)"
                  class="add-member-btn"
                  :icon="Plus"
                />
              </div>
              <div class="task-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click.stop="handleProcess(task)"
                  :disabled="isAdmin.value && !canUserModifyTask(task)"
                  :title="isAdmin.value && !canUserModifyTask(task) ? 'Only task members can modify this task' : ''"
                >
                  Go to process
                </el-button>
                <el-button type="primary" plain size="small" @click.stop="handleComments(task)">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>Comments({{ getTaskCommentCount(task) }})</span>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 进行中任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'in-process'" class="task-section">
        <div class="section-header">
          <h2>In progress</h2>
          <span class="task-count">{{ inProcessTasks.length }}</span>
        </div>
        <div class="task-row">
          <el-card 
            v-for="task in inProcessTasks" 
            :key="task.id" 
            class="task-card"
            :data-priority="getPriorityName(task.priority)"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title || task.name }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ getPriorityName(task.priority) }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName || '未知项目' }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="task-info">
              <p><span class="label">Create time:</span> {{ task.createTime }}</p>
              <p><span class="label">Start time:</span> {{ task.startTime }}</p>
              <p>
                <span class="label">Due time:</span> 
                <span :class="{ 'expired-date': isTaskExpired(task) }">{{ task.deadline || task.dueTime }}</span>
              </p>
            </div>
            <div class="task-tags">
              <el-tag
                v-for="tag in task.tags"
                :key="tag"
                class="task-tag"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="task-members">
              <el-tooltip
                v-for="member in task.members"
                :key="member"
                :content="member"
                placement="top"
                effect="light"
                :show-after="200"
              >
                <el-avatar
                  :size="28"
                  class="member-avatar"
                  @click.stop="isLeader && handleRemoveMember(task, member)"
                  :title="isLeader ? 'Click to remove member' : member"
                >
                  {{ member.charAt(0) }}
                </el-avatar>
              </el-tooltip>
              <el-button
                v-if="isLeader"
                circle
                size="small"
                @click.stop="handleAddMember(task)"
                class="add-member-btn"
                :icon="Plus"
              />
            </div>
            <div class="task-actions">
              <el-button 
                type="success" 
                size="small" 
                @click.stop="handleFinish(task)"
                :disabled="isAdmin.value && !canUserModifyTask(task)"
                :title="isAdmin.value && !canUserModifyTask(task) ? 'Only task members can modify this task' : ''"
              >
                Complete
              </el-button>
              <el-button type="primary" plain size="small" @click.stop="handleComments(task)">
                <el-icon><ChatLineRound /></el-icon>
                <span>Comments({{ getTaskCommentCount(task) }})</span>
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 已完成任务 -->
      <div v-show="activeTab === 'all' || activeTab === 'completed'" class="task-section">
        <div class="section-header">
          <h2>Completed</h2>
          <span class="task-count">{{ completedTasks.length }}</span>
        </div>
        <div class="task-row">
          <el-card 
            v-for="task in completedTasks" 
            :key="task.id" 
            class="task-card"
            :data-priority="getPriorityName(task.priority)"
            @click="handleTaskClick(task)"
          >
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <span class="task-title">{{ task.title || task.name }}</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ getPriorityName(task.priority) }}
                  </el-tag>
                </div>
                <div class="project-info">
                  <el-tag size="small" effect="plain" class="project-tag">
                    <el-icon><Folder /></el-icon>
                    {{ task.projectName || 'Unknown Project' }}
                  </el-tag>
                </div>
              </div>
            </template>
            <div class="task-info">
              <p><span class="label">Create time:</span> {{ task.createTime }}</p>
              <p><span class="label">Start time:</span> {{ task.startTime }}</p>
              <p>
                <span class="label">Due time:</span> 
                <span :class="{ 'expired-date': isTaskExpired(task) }">{{ task.deadline || task.dueTime }}</span>
              </p>
            </div>
            <div class="task-tags">
              <el-tag
                v-for="tag in task.tags"
                :key="tag"
                class="task-tag"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="task-members">
              <el-tooltip
                v-for="member in task.members"
                :key="member"
                :content="member"
                placement="top"
                effect="light"
                :show-after="200"
              >
                <el-avatar
                  :size="28"
                  class="member-avatar"
                >
                  {{ member.charAt(0) }}
                </el-avatar>
              </el-tooltip>
            </div>
            <div class="task-actions">
              <el-button type="default" size="small" @click.stop="handleCheck(task)">
                <el-icon><View /></el-icon>
                <span>View</span>
              </el-button>
              <el-button type="primary" plain size="small" @click.stop="handleComments(task)">
                <el-icon><ChatLineRound /></el-icon>
                <span>Comments({{ getTaskCommentCount(task) }})</span>
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Search, Plus, Check, ChatLineRound, View, Folder } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { getAllProjects, getAllAdminProjects } from '../../api/project'
import { getTaskList, getProjectTasks, updateTask, getTaskComments, getAllUserTasks, getUserTasksByStatus, getCurrentUserTaskStats } from '../../api/task'
import type { TaskDetail, Project } from '../../types/task'
import dayjs from 'dayjs'

const userStore = useUserStore()
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

const isAdmin = computed(() => {
  return userStore.userInfo?.role === 2
})

const router = useRouter()

// 当前激活的标签页
const activeTab = ref<'all' | 'pending' | 'in-process' | 'completed'>('all')
// 搜索文本
const searchText = ref('')

// 项目相关数据
const projectList = ref<Project[]>([])
const selectedProject = ref('')

// 任务数据
const tasks = ref<TaskDetail[]>([])
const loading = ref(false)

// 添加一个显示全局任务的开关（仅管理员可见）
const showAllTasks = ref(false)

// 获取项目列表
const fetchProjects = async () => {
  try {
    // 针对管理员视图使用不同的API
    let res;
    
    if (isAdmin.value && showAllTasks.value) {
      console.log('管理员全局视图: 使用getAllAdminProjects API');
      res = await getAllAdminProjects();
    } else {
      console.log('普通用户视图: 使用getAllProjects API');
      res = await getAllProjects();
    }
    
    console.log('项目列表API响应:', res);
    
    if (res && res.data) {
      let tempProjectList: Project[] = [];
      
      // 根据实际API响应结构调整
      if (res.data.code === 1 && res.data.data) {
        if (Array.isArray(res.data.data)) {
          tempProjectList = res.data.data;
        } else if (res.data.data.items && Array.isArray(res.data.data.items)) {
          tempProjectList = res.data.data.items;
        }
      } else if (Array.isArray(res.data)) {
        tempProjectList = res.data;
      } else if (res.data.items && Array.isArray(res.data.items)) {
        tempProjectList = res.data.items;
      } else {
        console.error('项目数据格式不符合预期:', res.data)
        tempProjectList = [];
      }
      
      // 确保tempProjectList是数组后再调用map
      if (Array.isArray(tempProjectList)) {
        // 处理项目列表，确保ID为字符串
        const processedProjects = tempProjectList.map(project => ({
          ...project,
          id: String(project.id) // 确保id是字符串
        }));
        
        // 使用类型断言避免TypeScript错误
        projectList.value = processedProjects as any;
      } else {
        console.error('项目列表不是数组:', tempProjectList);
        projectList.value = [];
      }
      
      console.log('获取到的项目列表:', projectList.value)
      
      // 如果是管理员且项目列表为空，可以考虑强制获取所有项目
      if (isAdmin.value && showAllTasks.value && projectList.value.length === 0) {
        console.warn('管理员项目列表为空，尝试强制获取所有项目');
        try {
          // 使用直接的API调用而不是添加force参数
          const forceRes = await getAllAdminProjects();
          if (forceRes && forceRes.data) {
            let forceProjectList: Project[] = [];
            
            // 以下逻辑与上面相同，处理不同的返回格式
            if (forceRes.data.code === 1 && forceRes.data.data) {
              if (Array.isArray(forceRes.data.data)) {
                forceProjectList = forceRes.data.data;
              } else if (forceRes.data.data.items && Array.isArray(forceRes.data.data.items)) {
                forceProjectList = forceRes.data.data.items;
              }
            } else if (Array.isArray(forceRes.data)) {
              forceProjectList = forceRes.data;
            } else if (forceRes.data.items && Array.isArray(forceRes.data.items)) {
              forceProjectList = forceRes.data.items;
            }
            
            if (Array.isArray(forceProjectList) && forceProjectList.length > 0) {
              projectList.value = forceProjectList.map(project => ({
                ...project,
                id: String(project.id)
              })) as any;
              console.log('强制获取的项目列表:', projectList.value);
            }
          }
        } catch (forceError) {
          console.error('强制获取项目列表失败:', forceError);
        }
      }
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('Get project list failed')
  }
}

// 获取当前用户或全部任务列表
const fetchTasks = async () => {
  loading.value = true;
  try {
    // 确保先加载项目列表
    if (projectList.value.length === 0) {
      console.log('任务加载前确保项目列表已加载');
      await fetchProjects();
    }
    
    let taskData: TaskDetail[] = [];
    
    if (selectedProject.value) {
      // 获取特定项目的任务
      const res = await getProjectTasks(selectedProject.value, {
        keyword: searchText.value || undefined
      });
      console.log('项目任务API响应:', res);
      
      if (res && res.data) {
        // 统一处理项目任务数据
        const responseData = res.data as any;
        
        // 标准响应格式：res.data.data.items
        if (responseData.data && responseData.data.items) {
          taskData = responseData.data.items;
        } 
        // 直接返回items格式
        else if (responseData.items) {
          taskData = responseData.items;
        }
        // 直接返回数组格式
        else if (Array.isArray(responseData)) {
          taskData = responseData;
        }
      }
    } else if (isAdmin.value && showAllTasks.value) {
      // 管理员查看所有任务
      const res = await getTaskList({
        keyword: searchText.value || undefined
      });
      console.log('全部任务API响应:', res);
      
      if (res && res.data) {
        // 统一处理全部任务数据
        const responseData = res.data as any;
        
        // 标准响应格式：res.data.data.items
        if (responseData.data && responseData.data.items) {
          taskData = responseData.data.items;
        } 
        // 直接返回items格式
        else if (responseData.items) {
          taskData = responseData.items;
        }
        // 直接返回数组格式
        else if (Array.isArray(responseData)) {
          taskData = responseData;
        }
        
        // 为管理员全局视图处理项目关联
        if (taskData.length > 0 && projectList.value.length > 0) {
          taskData = processAdminGlobalTasks(taskData);
        }
      }
    } else {
      // 获取当前用户的所有任务
      const res = await getAllUserTasks();
      console.log('用户任务API响应:', res);
      
      if (res && res.data) {
        // 统一处理用户任务数据
        const responseData = res.data as any;
        
        // 标准响应格式：res.data.data.items
        if (responseData.data && responseData.data.items) {
          taskData = responseData.data.items;
        } 
        // 直接返回items格式
        else if (responseData.items) {
          taskData = responseData.items;
        }
        // 直接返回数组格式
        else if (Array.isArray(responseData)) {
          taskData = responseData;
        }
      }
    }
    
    // 确保任务数据是数组
    tasks.value = Array.isArray(taskData) ? taskData : []
    
    try {
      // 为任务补充项目名称
      enrichTasksWithProjectNames();
    } catch (enrichError) {
      console.error('补充项目名称时出错:', enrichError);
      // 即使补充名称失败，也继续处理其他逻辑
    }
    
    console.log('处理后的任务数据:', tasks.value)
    
    // 获取每个任务的评论数量
    fetchCommentCounts()
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 根据projectId为任务补充项目名称
const enrichTasksWithProjectNames = () => {
  // 创建项目ID到项目名称的映射
  const projectMap = new Map<string, string>();
  
  if (projectList.value.length === 0) {
    console.warn('项目列表为空，无法补充项目名称');
    // 仍然继续处理任务，只是使用默认的项目名称
    tasks.value.forEach(task => {
      if (!task.projectName || task.projectName === '') {
        task.projectName = 'Unknown Project';
      }
    });
    return;
  }
  
  console.log('开始为任务补充项目名称，项目列表长度:', projectList.value.length);
  
  // 确保所有项目ID都转换为字符串
  projectList.value.forEach(project => {
    const projectId = String(project.id);
    projectMap.set(projectId, project.name);
    console.log(`项目映射: ID ${projectId} -> ${project.name}`);
  });
  
  // 为每个任务补充项目名称
  tasks.value.forEach(task => {
    // 确保将projectId转换为字符串再比较
    const taskProjectId = task.projectId ? String(task.projectId) : '';
    
    console.log(`处理任务: ${task.id}, 项目ID: ${taskProjectId}, 当前项目名称: ${task.projectName || '无'}`);
    
    if (taskProjectId && (!task.projectName || task.projectName === '' || task.projectName === 'Unknown Project')) {
      const projectName = projectMap.get(taskProjectId);
      if (projectName) {
        console.log(`-> 找到匹配项目: ${projectName}`);
        task.projectName = projectName;
      } else {
        console.log(`-> 未找到匹配项目, 设为Unknown Project`);
        task.projectName = 'Unknown Project';
      }
    } else if (!taskProjectId) {
      console.log(`-> 任务无项目ID`);
    }
  });
  
  console.log('项目名称补充完成');
}

// 获取所有任务的评论数量
const commentCounts = ref<Record<string, number>>({})

const fetchCommentCounts = async () => {
  // 对于每个没有commentCount字段的任务，使用缓存的评论数量
  for (const task of tasks.value) {
    if (task.commentCount === undefined && commentCounts.value[task.id] === undefined) {
      // 如果任务数据中已有评论列表，直接使用其长度
      if (task.comments && Array.isArray(task.comments)) {
        commentCounts.value[task.id] = task.comments.length;
      } else {
        // 没有评论数据时默认为0
        commentCounts.value[task.id] = 0;
      }
    }
  }
}

// 获取任务评论数量
const getTaskCommentCount = (task: TaskDetail): number => {
  // 直接使用任务数据中的commentCount字段
  if (task.commentCount !== undefined) {
    return task.commentCount;
  }
  
  // 如果有评论数组，使用数组长度
  if (task.comments && Array.isArray(task.comments)) {
    return task.comments.length;
  }
  
  // 如果有缓存的评论数量，返回缓存的数量
  if (commentCounts.value[task.id] !== undefined) {
    return commentCounts.value[task.id];
  }
  
  // 默认返回0
  return 0;
}

// 处理项目选择变更
const handleProjectChange = () => {
  fetchTasks()
}

// 监听搜索文本变化
watch(searchText, () => {
  fetchTasks()
})

// 监听全局任务视图开关变化
watch(showAllTasks, async (newValue) => {
  console.log('全局任务视图切换:', newValue);
  if (newValue && isAdmin.value) {
    // 重新获取项目列表，确保有最新数据
    await fetchProjects();
  }
  // 重新获取任务列表
  fetchTasks();
})

// 在组件挂载时初始化数据
onMounted(async () => {
  console.log('组件挂载，用户角色:', userStore.userInfo?.role);
  
  // 初始化管理员全局任务视图
  if (isAdmin.value) {
    console.log('管理员用户，初始化全局视图设置');
    showAllTasks.value = true; // 默认为管理员开启全局视图
  }
  
  await fetchProjects(); // 先获取项目列表
  await fetchTasks();    // 再获取当前用户的任务列表
})

// 按状态分组的任务列表
const pendingTasks = computed(() => {
  return tasks.value.filter(task => task.status === 0)
})

const inProcessTasks = computed(() => {
  return tasks.value.filter(task => task.status === 1)
})

const completedTasks = computed(() => {
  return tasks.value.filter(task => task.status === 2)
})

// 检查当前用户是否可以操作此任务
const canUserModifyTask = (task: TaskDetail): boolean => {
  // 如果是项目负责人，可以操作任务
  if (isLeader.value) return true;
  
  // 对于管理员用户，默认规则是：仅有任务所属项目的成员才能修改任务
  // 判断当前用户是否在任务成员列表中
  const currentUsername = userStore.userInfo?.username;
  if (currentUsername && task.members.includes(currentUsername)) {
    return true;
  }
  
  return false;
}

// 获取优先级名称
const getPriorityName = (priority: number): string => {
  const priorities = {
    1: 'LOW',
    2: 'MEDIUM',
    3: 'HIGH',
    4: 'CRITICAL'
  }
  return priorities[priority as keyof typeof priorities] || 'MEDIUM'
}

// 获取优先级对应的标签类型
const getPriorityType = (priority: number): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const types = {
    4: 'danger',   // CRITICAL
    3: 'warning',  // HIGH
    2: 'primary',  // MEDIUM
    1: 'info'      // LOW
  } as const
  return types[priority as keyof typeof types] || 'info'
}

// 处理任务操作
const handleFinish = async (task: TaskDetail) => {
  try {
    await ElMessageBox.confirm('Are you sure to mark the task as completed?', 'Tips', {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    
    await updateTask(task.id.toString(), { status: 2 })
    task.status = 2
    ElMessage.success('Task completed')
    fetchTasks() // 重新获取最新数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Update task status failed:', error)
      ElMessage.error('Operation failed')
    }
  }
}

const handleCheck = (task: TaskDetail) => {
  router.push(`/detail/${task.id}`)
}

const handleComments = (task: TaskDetail) => {
  router.push(`/detail/${task.id}?tab=comments`)
}

const handleAddMember = (task: TaskDetail) => {
  // 使用tab查询参数导航到任务详情页
  router.push(`/detail/${task.id}?tab=members`);
}

const handleRemoveMember = (task: TaskDetail, member: string) => {
  ElMessageBox.confirm(`Confirm to remove ${member} from the task?`, 'Tips', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    const index = task.members.indexOf(member)
    if (index !== -1) {
      const updatedMembers = [...task.members]
      updatedMembers.splice(index, 1)
      
      try {
        await updateTask(task.id.toString(), { members: updatedMembers })
        task.members = updatedMembers
        ElMessage.success('Member removed')
        fetchTasks() // 重新获取最新数据
      } catch (error) {
        console.error('Remove member failed:', error)
        ElMessage.error('Operation failed')
      }
    }
  }).catch(() => {})
}

const handleCreateTask = () => {
  router.push('/task/create')
}

const handleNavigateToCreate = () => {
  router.push('/task/create')
}

const handleTaskClick = (task: TaskDetail) => {
  router.push(`/detail/${task.id}`)
}

const handleProcess = async (task: TaskDetail) => {
  try {
    await ElMessageBox.confirm('Confirm to mark the task as in progress?', 'Tips', {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    
    await updateTask(task.id.toString(), { status: 1 })
    task.status = 1
    ElMessage.success('Task started')
    fetchTasks() // 重新获取最新数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Update task status failed:', error)
      ElMessage.error('Operation failed')
    }
  }
}

// 检查任务是否已过期
const isTaskExpired = (task: TaskDetail): boolean => {
  if (!task.deadline && !task.dueTime) return false;
  
  const deadlineStr = task.deadline || task.dueTime;
  if (!deadlineStr) return false;
  
  const deadline = new Date(deadlineStr);
  const now = new Date();
  
  // 如果任务未完成且已过截止日期，则认为已过期
  return task.status !== 2 && deadline < now;
}

// 处理Admin全局视图特殊逻辑
const processAdminGlobalTasks = (taskData: TaskDetail[]) => {
  // 从任务中提取所有的项目ID
  const projectIdsFromTasks = new Set<string>();
  taskData.forEach(task => {
    if (task.projectId) {
      projectIdsFromTasks.add(String(task.projectId));
    }
  });
  
  console.log('从任务中提取的唯一项目ID:', Array.from(projectIdsFromTasks));
  
  // 检查是否有项目ID不在当前项目列表中
  const projectIdsInList = new Set<string>();
  projectList.value.forEach(project => {
    projectIdsInList.add(String(project.id));
  });
  
  // 找出缺失的项目ID
  const missingProjectIds = Array.from(projectIdsFromTasks).filter(id => !projectIdsInList.has(id));
  
  if (missingProjectIds.length > 0) {
    console.log('缺失的项目ID:', missingProjectIds);
    
    // 为缺失的项目创建临时项目对象
    const temporaryProjects: Project[] = missingProjectIds.map(id => ({
      id: parseInt(id), // 确保id是数字类型
      name: `Project #${id}`,
      description: 'Auto-generated placeholder for missing project',
      status: 1, // 活跃状态
      priority: 2, // 中等优先级
      startTime: new Date().toISOString(),
      endTime: new Date().toISOString(),
      createTime: new Date().toISOString(),
      createUser: 0, // 系统用户
      updateTime: new Date().toISOString(),
      updateUser: 0,
      members: []
    }));
    
    // 添加到项目列表中
    projectList.value = [...projectList.value, ...temporaryProjects];
    console.log('添加临时项目后的项目列表:', projectList.value);
  }
  
  return taskData;
};
</script>

<style scoped>
.kanban-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  flex-wrap: wrap;
  gap: 20px;
}

.search-input {
  width: 360px;
}

.kanban-board {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.task-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.task-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 16px;
  overflow-x: auto;
}

.task-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 320px;
  width: 320px;
  margin: 10px;
  border: 1px solid #ebeef5;
  border-left: 4px solid transparent;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.task-card-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-project {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.task-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-dates {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.task-members {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-actions {
  display: flex;
  gap: 8px;
}

.priority-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.priority-urgent {
  background-color: #fef0f0;
  color: #f56c6c;
}

.priority-high {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.priority-medium {
  background-color: #ecf5ff;
  color: #409eff;
}

.priority-low {
  background-color: #f0f9eb;
  color: #67c23a;
}

.add-task-card {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 320px;
  width: 320px;
  margin: 10px;
  background: #fafafa;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-task-card:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.add-task-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  padding: 0;
}

.add-task-content .el-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.add-task-content p {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.task-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 20px;
  justify-items: center;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 标签样式 */
.task-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.task-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: #f4f4f5;
  color: #909399;
}

/* 状态标签样式 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background-color: #f4f4f5;
  color: #909399;
}

.status-in-progress {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

/* 头像样式统一 */
.task-members .member-avatar {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #fff;
  background: var(--el-color-primary-light-3);
  color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-right: -12px;
  font-size: 13px;
  font-weight: 500;
}

.task-members .member-avatar:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
  z-index: 2;
  border-color: var(--el-color-primary-light-5);
}

.task-members .member-avatar:not(:first-child) {
  margin-left: 0;
}

.task-actions .el-button {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
}

.task-actions .el-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.task-actions .el-button span {
  visibility: visible !important;
  opacity: 1 !important;
}

.task-actions .el-button:hover {
  opacity: 0.9;
}

.task-actions .el-button:hover span {
  visibility: visible !important;
  opacity: 1 !important;
}

/* 优化分组数量统计样式 */
.task-count {
  margin-left: 12px;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.1);
}

/* 优化卡片内部布局 */
.task-card :deep(.el-card__header) {
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
}

.task-card :deep(.el-card__body) {
  padding: 8px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  font-size: 12px;
  padding: 0;
}

.card-content p {
  margin: 2px 0;
  color: var(--el-text-color-regular);
  line-height: 1.3;
}

.tags-container {
  margin: 4px 0;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.task-members {
  margin: 4px 0;
  padding: 4px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.task-actions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  gap: 8px;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-info {
  display: flex;
  align-items: center;
}

.project-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-color: var(--el-border-color-lighter);
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
}

.task-info {
  margin: 4px 0;
  line-height: 1.3;
}

.task-info p {
  margin: 2px 0;
  font-size: 12px;
}

.task-info .label {
  font-weight: 500;
  margin-right: 4px;
  color: var(--el-text-color-secondary);
}

.task-tags {
  margin: 8px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.task-tags .el-tag {
  margin: 2px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
}

/* 新增任务卡片样式优化 */
.add-task-card {
  height: 320px;
}

.add-task-content {
  padding: 20px;
  margin-top: 20px;
  margin-bottom: 20px;
  
}

.add-task-content .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  margin-top: 16px;
}

.add-task-content p {
  font-size: 18px;
  margin-top: 16px;
  margin-bottom: 16px;
}

/* 优化按钮样式 */
.task-actions .el-button {
  height: 28px;
  padding: 0 12px;
  font-size: 12px;
}

.task-actions .el-button .el-icon {
  font-size: 14px;
  margin-right: 4px;
}

/* 优化头像样式 */
.task-members .member-avatar {
  width: 24px;
  height: 24px;
  font-size: 12px;
  margin-right: -8px;
}

.add-member-btn {
  width: 24px !important;
  height: 24px !important;
  margin-left: 4px !important;
}

.add-member-btn .el-icon {
  font-size: 12px;
}

/* 优化头部样式 */
.card-header {
  gap: 4px;
}

.header-main {
  margin-bottom: 4px;
}

.project-select {
  width: 200px;
  margin-right: 16px;
}

/* 管理员全局视图开关样式 */
.admin-switch {
  display: flex;
  align-items: center;
  margin-left: 20px;
  padding: 0 10px;
  height: 40px;
  gap: 24px;     
}

.admin-switch span {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.admin-switch :deep(.el-switch) {
  --el-switch-on-color: #409EFF;
  --el-switch-off-color: #909399;
}

/* 优化滚动条样式 */
.task-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 4px;
}

.task-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: var(--el-border-color-lighter);
  border-radius: 2px;
}

.task-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: transparent;
}

.task-card[data-priority="CRITICAL"] {
  border-left-color: #F56C6C;
}

.task-card[data-priority="HIGH"] {
  border-left-color: #E6A23C;
}

.task-card[data-priority="MEDIUM"] {
  border-left-color: #409EFF;
}

.task-card[data-priority="LOW"] {
  border-left-color: #67C23A;
}

.expired-date {
  color: #f56c6c;
  font-weight: 600;
}

.kanban-tabs {
  flex-grow: 0;
  min-width: 300px;
  max-width: 450px;
}

.tabs-container {
  display: flex;
  align-items: center;
  width: auto;
}

/* 适应不同屏幕宽度 */
@media (max-width: 768px) {
  .kanban-tabs {
    min-width: 250px;
    max-width: 100%;
  }
  
  .tabs-container {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>
