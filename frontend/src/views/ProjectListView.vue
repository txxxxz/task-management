<template>
  <div class="project-list">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="handleCreate">
        创建项目
      </el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="project in projectStore.projects" :key="project.id" :span="8">
        <el-card class="project-card" :body-style="{ padding: '0px' }">
          <div class="project-header">
            <el-tag :type="getStatusType(project.status)">
              {{ getStatusText(project.status) }}
            </el-tag>
          </div>
          <div class="project-body">
            <h3 class="project-title">{{ project.name }}</h3>
            <p class="project-desc">{{ project.description }}</p>
          </div>
          <div class="project-footer">
            <div class="project-info">
              <span>创建人：{{ project.creator?.username }}</span>
              <span>创建时间：{{ formatDate(project.createTime) }}</span>
            </div>
            <div class="project-actions">
              <el-button text @click="$router.push(`/projects/${project.id}`)">
                查看详情
              </el-button>
              <el-dropdown trigger="click">
                <el-button text>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(project)">
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleDelete(project)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑项目' : '创建项目'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择项目状态">
            <el-option label="进行中" :value="0" />
            <el-option label="已暂停" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { More } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useProjectStore } from '@/store/project'
import type { Project } from '@/types/models'

const projectStore = useProjectStore()
const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const loading = ref(false)
const form = ref<Partial<Project>>({
  name: '',
  description: '',
  status: 0
})

const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择项目状态', trigger: 'change' }
  ]
}

onMounted(() => {
  projectStore.fetchProjects()
})

const handleCreate = () => {
  form.value = {
    name: '',
    description: '',
    status: 0
  }
  dialogVisible.value = true
}

const handleEdit = (project: Project) => {
  form.value = {
    id: project.id,
    name: project.name,
    description: project.description,
    status: project.status
  }
  dialogVisible.value = true
}

const handleDelete = (project: Project) => {
  ElMessageBox.confirm(
    '确认删除该项目吗？删除后无法恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await projectStore.deleteProject(project.id)
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (form.value.id) {
          await projectStore.updateProject(form.value.id, form.value)
        } else {
          await projectStore.createProject(form.value)
        }
        dialogVisible.value = false
      } finally {
        loading.value = false
      }
    }
  })
}

const getStatusType = (status: number): 'success' | 'warning' | 'info' | 'primary' | 'danger' | '' => {
  const types = {
    0: 'primary',
    1: 'warning',
    2: 'success'
  } as const
  return types[status as keyof typeof types] || ''
}

const getStatusText = (status: number) => {
  const texts = {
    0: '进行中',
    1: '已暂停',
    2: '已完成'
  }
  return texts[status as keyof typeof texts]
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
.project-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}

.project-card {
  margin-bottom: 16px;
  transition: all 0.3s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.project-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.project-body {
  padding: 16px;
}

.project-title {
  margin: 0 0 8px;
  font-size: 16px;
}

.project-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.project-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.project-actions {
  display: flex;
  gap: 8px;
}
</style> 