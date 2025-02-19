<template>
  <div class="project-list-container">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="项目名称">
            <el-input v-model="searchForm.name" placeholder="请输入项目名称" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="成员">
            <el-select v-model="searchForm.members" multiple placeholder="请选择成员" clearable style="width: 100%">
              <el-option
                v-for="item in memberOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24" style="text-align: right">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="primary" @click="handleCreate" v-if="isLeader">
            <el-icon><Plus /></el-icon> 新建项目
          </el-button>
        </el-col>
      </el-row>
    </el-form>

    <!-- 项目列表 -->
    <el-table
      v-loading="loading"
      :data="filteredProjects"
      style="width: 100%; margin-top: 20px"
      border
      :header-cell-style="{
        background: '#f5f7fa',
        color: '#606266',
        height: '50px',
        fontWeight: 600
      }"
      :cell-style="{
        padding: '12px 0'
      }"
    >
      <el-table-column prop="name" label="项目名称" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '进行中' : '已归档' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="成员" width="200">
        <template #default="{ row }">
          <el-tooltip
            :content="row.members.join(', ')"
            placement="top"
            :show-after="200"
          >
            <div class="member-list">
              {{ row.members.join(', ') }}
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button-group class="operation-group">
            <el-tooltip content="编辑" placement="top" v-if="isLeader">
              <el-button type="primary" link @click="handleProjectAction('edit', row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="成员管理" placement="top" v-if="isLeader">
              <el-button type="primary" link @click="handleMemberManagement(row)">
                <el-icon><User /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="归档" placement="top" v-if="isLeader && row.status === 'ACTIVE'">
              <el-button type="warning" link @click="handleProjectAction('archive', row)">
                <el-icon><Folder /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" v-if="isLeader && row.status === 'ARCHIVED'">
              <el-button type="danger" link @click="handleProjectAction('delete', row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
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

    <!-- 成员管理对话框 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="成员管理"
      width="400px"
    >
      <div class="member-list">
        <div v-for="member in currentProjectMembers" :key="member" class="member-item">
          <el-avatar :size="32">{{ member.charAt(0) }}</el-avatar>
          <span class="member-name">{{ member }}</span>
          <el-button
            type="danger"
            link
            @click="handleRemoveMember(member)"
            v-if="isLeader"
          >
            移除
          </el-button>
        </div>
      </div>
      <div class="add-member" v-if="isLeader">
        <el-select
          v-model="newMember"
          filterable
          placeholder="添加成员"
          class="member-select"
        >
          <el-option
            v-for="user in availableUsers"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          />
        </el-select>
        <el-button type="primary" @click="handleAddMember">添加</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ProjectStatus } from '@/utils/status'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  User,
  Folder
} from '@element-plus/icons-vue'
import type { Project } from '@/types/task'
import {
  getProjectList,
  deleteProject,
  updateProject,
  addProjectMember,
  removeProjectMember,
  getProjectMembers
} from '@/api/project'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 判断是否为项目负责人
const isLeader = computed(() => {
  return userStore.userInfo?.role === 1
})

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  members: [] as string[]
})

// 项目列表数据
const projectList = ref<Project[]>([])

// 选项数据
const statusOptions = [
  { label: '进行中', value: 'ACTIVE' },
  { label: '已归档', value: 'ARCHIVED' }
]

const memberOptions = [
  { label: 'Tom', value: 'Tom' },
  { label: 'John', value: 'John' },
  { label: 'Amy', value: 'Amy' },
  { label: 'Jack', value: 'Jack' }
]

// 成员管理相关
const memberDialogVisible = ref(false)
const currentProjectMembers = ref<string[]>([])
const newMember = ref('')
const currentProjectId = ref('')

// 可添加的用户列表
const availableUsers = computed(() => {
  return memberOptions.filter(
    user => !currentProjectMembers.value.includes(user.value)
  )
})

// 过滤后的项目列表
const filteredProjects = computed(() => {
  return projectList.value.filter(project => {
    if (searchForm.name && !project.name.toLowerCase().includes(searchForm.name.toLowerCase())) return false
    if (searchForm.status && project.status !== Number(searchForm.status)) return false
    if (searchForm.members.length && !searchForm.members.some(member => project.members.includes(member))) return false
    return true
  })
})

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await getProjectList()
    if (response.data && response.data.code === 200) {
      projectList.value = response.data.data.items
      total.value = response.data.data.total
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

// 处理重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: '',
    members: []
  })
  handleSearch()
}

// 处理新建项目
const handleCreate = () => {
  router.push('/project/create')
}

// 处理项目操作
const handleProjectAction = async (action: string, project: Project) => {
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人可以执行此操作')
    return
  }

  try {
    switch (action) {
      case 'edit':
        router.push(`/project/edit/${project.id}`)
        break
      case 'archive':
        const archiveResponse = await updateProject(project.id, { 
          status: 3 as ProjectStatus 
        })
        if (archiveResponse.data.code === 200) {
          ElMessage.success('项目已归档')
          fetchProjects()
        }
        break
      case 'delete':
        const deleteResponse = await deleteProject(project.id)
        if (deleteResponse.data.code === 200) {
          ElMessage.success('项目已删除')
          fetchProjects()
        }
        break
      default:
        break
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理查看详情
const handleViewDetail = (row: Project) => {
  router.push(`/project/detail/${row.id}`)
}

// 处理成员管理
const handleMemberManagement = async (project: Project) => {
  if (!isLeader.value) {
    ElMessage.warning('只有项目负责人可以管理成员')
    return
  }
  currentProjectId.value = project.id.toString()
  try {
    const response = await getProjectMembers(project.id)
    if (response.data.code === 200) {
      currentProjectMembers.value = response.data.data
      memberDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取项目成员失败:', error)
    ElMessage.error('获取项目成员失败')
  }
}

// 处理添加成员
const handleAddMember = async () => {
  if (!newMember.value) return
  
  try {
    const response = await addProjectMember(parseInt(currentProjectId.value), newMember.value)
    if (response.data.code === 200) {
      currentProjectMembers.value.push(newMember.value)
      newMember.value = ''
      ElMessage.success('添加成员成功')
    }
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error('添加成员失败')
  }
}

// 处理移除成员
const handleRemoveMember = async (member: string) => {
  try {
    await ElMessageBox.confirm('确定要移除该成员吗？', '提示', {
      type: 'warning'
    })
    const response = await removeProjectMember(parseInt(currentProjectId.value), member)
    if (response.data.code === 200) {
      const index = currentProjectMembers.value.indexOf(member)
      if (index !== -1) {
        currentProjectMembers.value.splice(index, 1)
      }
      ElMessage.success('移除成员成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchProjects()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProjects()
}

// 初始化
onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.project-list-container {
  padding: 20px;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.member-list {
  max-height: 300px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.member-name {
  margin-left: 12px;
  flex: 1;
}

.add-member {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.member-select {
  flex: 1;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-button-group) {
  .el-button--link {
    padding: 4px 8px;
  }
}

.operation-group {
  display: flex;
  justify-content: center;
  gap: 4px;
}

:deep(.el-table) {
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style> 