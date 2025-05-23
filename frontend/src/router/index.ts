import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: 'Login',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: 'Register',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: 'Dashboard',
          requiresAuth: true
        }
      },
      {
        path: 'gantt',
        name: 'gantt',
        component: () => import('@/views/gantt/gantt.vue'),
        meta: {
          title: 'Gantt Chart',
          requiresAuth: true
        }
      },
      {
        path: 'list',
        name: 'List',
        component: () => import('@/views/task/list.vue'),
        meta: {
          title: 'Task List',
          requiresAuth: true
        }
      },
      {
        path: 'kanban',
        name: 'kanban',
        component: () => import('@/views/kanban/index.vue'),
        meta: {
          title: 'Task Board',
          requiresAuth: true
        }
      },
      {
        path: 'create',
        name: 'CreateTask',
        component: () => import('@/views/task/form.vue'),
        meta: {
          title: 'Create Task',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail.vue'),
        meta: {
          title: 'Task Detail',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: 'Personal Centeronal Center',
          requiresAuth: true
        }
      },
      {
        path: 'task/create',
        name: 'TaskCreate',
        component: () => import('@/views/task/form.vue'),
        meta: {
          title: 'Create Task',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'task/edit/:id',
        name: 'TaskEdit',
        component: () => import('@/views/task/form.vue'),
        meta: {
          title: 'Edit Task',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'project/list',
        name: 'ProjectList',
        component: () => import('@/views/project/list.vue'),
        meta: {
          title: 'Project List',
          requiresAuth: true
        }
      },
      {
        path: 'project/create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/form.vue'),
        meta: {
          title: 'Create Project',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'project/edit/:id',
        name: 'ProjectEdit',
        component: () => import('@/views/project/form.vue'),
        meta: {
          title: 'Edit Project',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'project/detail/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/detail.vue'),
        meta: {
          title: 'Project Detail',
          requiresAuth: true
        }
      },
      {
        path: 'employee',
        name: 'EmployeeManagement',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: 'Employee Management',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'tag/list',
        name: 'TagList',
        component: () => import('@/views/tag/list.vue'),
        meta: {
          title: 'Tag List',
          requiresAuth: true
        }
      },
      {
        path: 'tag/create',
        name: 'TagCreate',
        component: () => import('@/views/tag/form.vue'),
        meta: {
          title: 'Create Tag',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'tag/edit/:id',
        name: 'TagEdit',
        component: () => import('@/views/tag/form.vue'),
        meta: {
          title: 'Edit Tag',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'tag/view/:id',
        name: 'TagView',
        component: () => import('@/views/tag/view.vue'),
        meta: {
          title: 'View Tag',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/exception/404.vue'),
    meta: {
      title: '404',
      requiresAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title} - Task Management System`

  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  // 判断该路由是否需要登录权限
  if (to.meta.requiresAuth) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 检查是否需要 leader 权限
    if (to.meta.requiresLeader && userStore.userInfo?.role !== 1) {
      //ElMessage.warning('You do not have permission to access this page')
      next('/dashboard')
      return
    }
    
    // 检查是否需要 admin 权限
    if (to.meta.requiresAdmin && userStore.userInfo?.role !== 2) {
      ElMessage.warning('You do not have permission to access this page')
      next('/dashboard')
      return
    }
    
    next()
  } else {
    next()
  }
})

export default router 