<template>
  <div class="project-container">
    <div class="project-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="handleCreateProject" v-if="isLeader">
        <el-icon><Plus /></el-icon> 新建项目
      </el-button>
    </div>

    <!-- 项目列表 -->
    <div class="project-list">
      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{ row }">
            <div class="project-name">
              <el-icon><Folder /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '进行中' : '已归档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                type="primary" 
                link 
                @click="handleEdit(row)"
                v-if="isLeader"
              >
                编辑
              </el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleMembers(row)"
                v-if="isLeader"
              >
                成员
              </el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleArchive(row)"
                v-if="isLeader && row.status === 'ACTIVE'"
              >
                归档
              </el-button>
              <el-button 
                type="danger" 
                link 
                @click="handleDelete(row)"
                v-if="isLeader && row.status === 'ARCHIVED'"
              >
                删除
              </el-button>
              <el-button 
                type="info" 
                link 
                @click="handleView(row)"
                v-if="!isLeader"
              >
                查看
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 项目表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formType === 'create' ? '新建项目' : '编辑项目'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="projectForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="成员" prop="members">
          <el-select
            v-model="projectForm.members"
            multiple
            filterable
            placeholder="请选择项目成员"
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
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

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
            :key="user.username"
            :label="user.nickname"
            :value="user.username"
          />
        </el-select>
        <el-button type="primary" @click="handleAddMember">添加</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Folder } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { ProjectStatus } from '@/utils/status'
import {
  getProjectList,
  createProject,
  updateProject,
  deleteProject,
  getProjectMembers,
  addProjectMember,
  removeProjectMember
} from '@/api/project'
import type { Project } from '@/types/task'
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
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 成员管理相关
const memberDialogVisible = ref(false)
const currentProjectMembers = ref<string[]>([])
const newMember = ref('')
const currentProjectId = ref(0)

// 模拟用户列表数据
const userList = ref([
  { username: 'admin', nickname: '管理员' },
  { username: 'user1', nickname: '用户1' },
  { username: 'user2', nickname: '用户2' }
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
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
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
            endTime: new Date().toISOString()
          })
          ElMessage.success('创建成功')
        } else {
          await updateProject(Number(projectForm.value.id), {
            name: projectForm.value.name,
            description: projectForm.value.description,
            members: projectForm.value.members
          })
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchProjects()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 处理归档项目
const handleArchive = async (row: Project) => {
  try {
    await ElMessageBox.confirm('确定要归档该项目吗？', '提示', {
      type: 'warning'
    })
    await updateProject(Number(row.id), { status: 3 as ProjectStatus })
    ElMessage.success('归档成功')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归档失败:', error)
      ElMessage.error('归档失败')
    }
  }
}

// 处理删除项目
const handleDelete = async (row: Project) => {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？此操作不可恢复！', '警告', {
      type: 'warning'
    })
    await deleteProject(Number(row.id))
    ElMessage.success('删除成功')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
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
    console.error('获取项目成员失败:', error)
    ElMessage.error('获取项目成员失败')
  }
}

// 处理添加成员
const handleAddMember = async () => {
  if (!newMember.value) return
  
  try {
    await addProjectMember(currentProjectId.value, newMember.value)
    currentProjectMembers.value.push(newMember.value)
    newMember.value = ''
    ElMessage.success('添加成员成功')
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error('添加成员失败')
  }
}

// 处理移除成员
const handleRemoveMember = async (username: string) => {
  try {
    await ElMessageBox.confirm('确定要移除该成员吗？', '提示', {
      type: 'warning'
    })
    await removeProjectMember(currentProjectId.value, username)
    const index = currentProjectMembers.value.indexOf(username)
    if (index !== -1) {
      currentProjectMembers.value.splice(index, 1)
    }
    ElMessage.success('移除成员成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
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