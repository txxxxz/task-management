<template>
  <div class="project-list">
    <div class="page-header">
      <h2>Project Management</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        New Project
      </el-button>
    </div>

    <el-table :data="projects" style="width: 100%">
      <el-table-column prop="name" label="Project Name" />
      <el-table-column prop="description" label="Project Description" />
      <el-table-column prop="status" label="Status">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" />
      <el-table-column label="Operation" width="200">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" link @click="handleEdit(row)">
              Edit
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              Delete
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑项目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEdit ? 'Edit Project' : 'New Project'"
      width="500px"
    >
      <el-form
        ref="projectForm"
        :model="projectForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="Project Name" prop="name">
          <el-input v-model="projectForm.name" />
        </el-form-item>
        <el-form-item label="Project Description" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Project {
  id: number
  name: string
  description: string
  status: string
  createTime: string
}

const projects = ref<Project[]>([])
const showCreateDialog = ref(false)
const isEdit = ref(false)

const projectForm = reactive({
  id: '',
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: 'Please enter the project name', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please enter the project description', trigger: 'blur' }
  ]
}

const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'primary' | 'danger' => {
  const statusMap = {
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'ON_HOLD': 'warning'
  } as const
  return statusMap[status as keyof typeof statusMap] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': 'In Progress',
    'COMPLETED': 'Completed',
    'ON_HOLD': 'On Hold'
  }
  return statusMap[status] || status
}

const handleEdit = (row: Project) => {
  isEdit.value = true
  Object.assign(projectForm, row)
  showCreateDialog.value = true
}

const handleDelete = (row: Project) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this project?',
    'Warning',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(() => {
    // TODO: 实现删除逻辑
    ElMessage.success('Delete successfully')
  })
}

const handleSubmit = async () => {
  try {
    // TODO: 实现提交逻辑
    showCreateDialog.value = false
    ElMessage.success(isEdit.value ? 'Edit successfully' : 'Create successfully')
  } catch (error) {
    ElMessage.error('Operation failed')
  }
}
</script>

<style scoped>
.project-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 