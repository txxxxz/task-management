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
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
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
          title: '仪表盘',
          requiresAuth: true
        }
      },
      {
        path: 'gantt',
        name: 'gantt',
        component: () => import('@/views/GanttView.vue'),
        meta: {
          title: '甘特图',
          requiresAuth: true
        }
      },
      {
        path: 'list',
        name: 'List',
        component: () => import('@/views/table/updateTask.vue'),
        meta: {
          title: '任务列表',
          requiresAuth: true
        }
      },
      {
        path: 'kanban',
        name: 'kanban',
        component: () => import('@/views/kanban/index.vue'),
        meta: {
          title: '任务看板',
          requiresAuth: true
        }
      },
      {
        path: 'create',
        name: 'CreateTask',
        component: () => import('@/views/table/createTask.vue'),
        meta: {
          title: '新增任务',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/table/taskDetail.vue'),
        meta: {
          title: '任务详情',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/index.vue'),
        meta: {
          title: '项目管理',
          requiresAuth: true
        }
      },
      {
        path: 'task/create',
        name: 'TaskCreate',
        component: () => import('@/views/task/form.vue'),
        meta: {
          title: '新建任务',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'task/edit/:id',
        name: 'TaskEdit',
        component: () => import('@/views/task/form.vue'),
        meta: {
          title: '编辑任务',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'project/list',
        name: 'ProjectList',
        component: () => import('@/views/project/list.vue'),
        meta: {
          title: '项目列表',
          requiresAuth: true
        }
      },
      {
        path: 'project/create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/form.vue'),
        meta: {
          title: '新增项目',
          requiresAuth: true,
          requiresLeader: true
        }
      },
      {
        path: 'project/edit/:id',
        name: 'ProjectEdit',
        component: () => import('@/views/project/form.vue'),
        meta: {
          title: '编辑项目',
          requiresAuth: true,
          requiresLeader: true
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
      ElMessage.warning('您没有权限访问该页面')
      next('/dashboard')
      return
    }
    
    next()
  } else {
    next()
  }
})

export default router 