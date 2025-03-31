<template>
  <div class="project-container">
    <div class="project-header">
      <h2>Project Management</h2>
      <el-button type="primary" @click="handleCreateProject" v-if="isLeader">
        <el-icon><Plus /></el-icon> Create Project
      </el-button>
    </div>

    <!-- 项目列表 -->
    <div class="project-list">
      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="Project Name" min-width="200">
          <template #default="{ row }">
            <div class="project-name">
              <el-icon><Folder /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="Description" show-overflow-tooltip />
        <el-table-column prop="leader" label="Leader" width="120" />
        <el-table-column prop="createTime" label="Create Time" width="180" />
        <el-table-column prop="status" label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? 'In Progress' : 'Archived' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Operations" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                type="primary" 
                link 
                @click="handleEdit(row)"
                v-if="isLeader"
              >
                Edit
              </el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleMembers(row)"
                v-if="isLeader"
              >
                Members
              </el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleArchive(row)"
                v-if="isLeader && row.status === 'ACTIVE'"
              >
                Archive
              </el-button>
              <el-button 
                type="danger" 
                link 
                @click="handleDelete(row)"
                v-if="isLeader && row.status === 'ARCHIVED'"
              >
                Delete
              </el-button>
              <el-button 
                type="info" 
                link 
                @click="handleView(row)"
                v-if="!isLeader"
              >
                View
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 项目表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formType === 'create' ? 'Create Project' : 'Edit Project'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="projectForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="Name" prop="name">
          <el-input v-model="projectForm.name" placeholder="Please enter the project name" />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
            placeholder="Please enter the project description"
          />
        </el-form-item>
        <el-form-item label="Members" prop="members">
          <el-select
            v-model="projectForm.members"
            multiple
            filterable
            placeholder="Please select project members"
          >
            <el-option
              v-for="user in userList"
              :key="user.username"
              :label="user.nickname"
              :value="user.username"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">Confirm</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="Members Management"
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
            Remove
          </el-button>
        </div>
      </div>
      <div class="add-member" v-if="isLeader">
        <el-select
          v-model="newMember"
          filterable
          placeholder="Add Member"
          class="member-select"
        >
          <el-option
            v-for="user in availableUsers"
            :key="user.username"
            :label="user.nickname"
            :value="user.username"
          />
        </el-select>
        <el-button type="primary" @click="handleAddMember">Add</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Folder } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '../../stores/user'
import type { ProjectStatus } from '../../utils/status'
import {
  getAllProjects as getProjectList,
  createProject,
  updateProject,
  deleteProject,
  getProjectMembers,
  addProjectMember,
  removeProjectMember
} from '../../api/project'
import type { Project } from '../../types/task'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const isLeader = computed(() => userStore.userInfo?.role === 1)

// 项目列表数据
const loading = ref(false)
const projectList = ref<Project[]>([])

// 表单相关
const dialogVisible = ref(false)
const formType = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const projectForm = ref({
  id: 0,
  name: '',
  description: '',
  members: [] as string[]
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: 'Please enter the project name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length must be between 2 and 50 characters', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: 'Cannot exceed 200 characters', trigger: 'blur' }
  ]
}

// 成员管理相关
const memberDialogVisible = ref(false)
const currentProjectMembers = ref<string[]>([])
const newMember = ref('')
const currentProjectId = ref(0)

// 模拟用户列表数据
const userList = ref([
  { username: 'admin', nickname: 'Administrator' },
  { username: 'user1', nickname: 'User 1' },
  { username: 'user2', nickname: 'User 2' }
])

// 计算可添加的用户列表
const availableUsers = computed(() => {
  return userList.value.filter(
    user => !currentProjectMembers.value.includes(user.username)
  )
})

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await getProjectList()
    projectList.value = response.data.data.items
  } catch (error) {
    console.error('Get project list failed:', error)
    ElMessage.error('Get project list failed')
  } finally {
    loading.value = false
  }
}

// 处理创建项目
const handleCreateProject = () => {
  formType.value = 'create'
  projectForm.value = {
    id: 0,
    name: '',
    description: '',
    members: []
  }
  dialogVisible.value = true
}

// 处理编辑项目
const handleEdit = (row: Project) => {
  formType.value = 'edit'
  projectForm.value = {
    id: Number(row.id),
    name: row.name,
    description: row.description || '',
    members: row.members
  }
  dialogVisible.value = true
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (formType.value === 'create') {
          await createProject({
            name: projectForm.value.name,
            description: projectForm.value.description,
            members: projectForm.value.members,
            startTime: new Date().toISOString(),
            endTime: new Date().toISOString(),
            status: 0,  // 0-筹备中
            priority: 2  // 2-中
          })
          ElMessage.success('Create successfully')
        } else {
          await updateProject(Number(projectForm.value.id), {
            name: projectForm.value.name,
            description: projectForm.value.description,
            members: projectForm.value.members
          })
          ElMessage.success('Update successfully')
        }
        dialogVisible.value = false
        fetchProjects()
      } catch (error) {
        console.error('Operation failed:', error)
        ElMessage.error('Operation failed')
      }
    }
  })
}

// 处理归档项目
const handleArchive = async (row: Project) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to archive this project?', 'Tips', {
      type: 'warning'
    })
    await updateProject(Number(row.id), { status: 3 as ProjectStatus })
    ElMessage.success('Archive successfully')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Archive failed:', error)
      ElMessage.error('Archive failed')
    }
  }
}

// 处理删除项目
const handleDelete = async (row: Project) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this project? This action cannot be undone!', 'Warning', {
      type: 'warning'
    })
    await deleteProject(Number(row.id))
    ElMessage.success('Delete successfully')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
      ElMessage.error('Delete failed')
    }
  }
}

// 处理成员管理
const handleMembers = async (row: Project) => {
  currentProjectId.value = row.id
  try {
    const response = await getProjectMembers(row.id)
    currentProjectMembers.value = response.data.data
    memberDialogVisible.value = true
  } catch (error) {
    console.error('Get project members failed:', error)
    ElMessage.error('Get project members failed')
  }
}

// 处理添加成员
const handleAddMember = async () => {
  if (!newMember.value) return
  
  try {
    await addProjectMember(currentProjectId.value, newMember.value)
    currentProjectMembers.value.push(newMember.value)
    newMember.value = ''
    ElMessage.success('Add member successfully')
  } catch (error) {
    console.error('Add member failed:', error)
    ElMessage.error('Add member failed')
  }
}

// 处理移除成员
const handleRemoveMember = async (username: string) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to remove this member?', 'Tips', {
      type: 'warning'
    })
    await removeProjectMember(currentProjectId.value, username)
    const index = currentProjectMembers.value.indexOf(username)
    if (index !== -1) {
      currentProjectMembers.value.splice(index, 1)
    }
    ElMessage.success('Successfully removed member')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Remove member failed:', error)
      ElMessage.error('Remove member failed')
    }
  }
}

// 添加查看详情方法
const handleView = (row: Project) => {
  // 跳转到项目详情页面
  router.push(`/projects/${row.id}`)
}

const router = useRouter()

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.project-container {
  padding: 20px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-name .el-icon {
  color: var(--el-color-primary);
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
</style> 