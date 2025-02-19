<template>
  <div class="step-form">
    <div class="step-header">
      <el-steps :active="currentStep" finish-status="success">
        <el-step 
          v-for="(step, index) in steps" 
          :key="index"
          :title="step.title"
          :status="getStepStatus(index)"
        />
      </el-steps>
    </div>

    <div class="step-content">
      <slot :name="'step-' + currentStep" />
    </div>

    <div class="step-footer">
      <el-button 
        v-if="currentStep > 0" 
        @click="handlePrevious"
      >
        上一步
      </el-button>
      <el-button 
        type="primary" 
        @click="handleNext"
        :loading="loading"
      >
        {{ isLastStep ? '完成' : '下一步' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Step {
  title: string
  component: string
}

interface Props {
  steps: Step[]
  currentStep: number
  loading?: boolean
}

interface Emits {
  (e: 'update:currentStep', step: number): void
  (e: 'previous'): void
  (e: 'next'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

const isLastStep = computed(() => props.currentStep === props.steps.length - 1)

const getStepStatus = (index: number) => {
  if (index < props.currentStep) return 'success'
  if (index === props.currentStep) return 'process'
  return 'wait'
}

const handlePrevious = () => {
  emit('previous')
  emit('update:currentStep', props.currentStep - 1)
}

const handleNext = () => {
  emit('next')
  if (!isLastStep.value) {
    emit('update:currentStep', props.currentStep + 1)
  }
}
</script>

<style scoped>
.step-form {
  padding: 24px;
}

.step-header {
  margin-bottom: 40px;
}

.step-content {
  min-height: 300px;
  margin-bottom: 40px;
}

.step-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style> 