import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('@/views/LayoutView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue')
        },
        {
          path: 'projects',
          name: 'projects',
          component: () => import('@/views/ProjectListView.vue')
        },
        {
          path: 'projects/:id',
          name: 'project-detail',
          component: () => import('@/views/ProjectDetailView.vue')
        },
        {
          path: 'projects/:projectId/tasks/:taskId',
          name: 'task-detail',
          component: () => import('@/views/TaskDetailView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue')
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (token && !userStore.user) {
    await userStore.getUserInfo()
  }

  if (to.meta.requiresAuth && !userStore.user) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (!to.meta.requiresAuth && userStore.user) {
    return { name: 'home' }
  }
})

export default router 