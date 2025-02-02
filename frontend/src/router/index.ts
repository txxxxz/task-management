import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

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
        path: 'workplace',
        name: 'Workplace',
        component: () => import('@/views/workplace/index.vue'),
        meta: {
          title: '工作台',
          requiresAuth: true
        }
      },
      {
        path: 'data',
        name: 'Data',
        component: () => import('@/views/data/index.vue'),
        meta: {
          title: '数据可视化',
          requiresAuth: true
        }
      },
      {
        path: 'list',
        name: 'List',
        component: () => import('@/views/list/index.vue'),
        meta: {
          title: '列表页',
          requiresAuth: true
        }
      },
      {
        path: 'table',
        name: 'Table',
        component: () => import('@/views/table/index.vue'),
        meta: {
          title: '表格页',
          requiresAuth: true
        }
      },
      {
        path: 'detail',
        name: 'Detail',
        component: () => import('@/views/detail/index.vue'),
        meta: {
          title: '详情页',
          requiresAuth: true
        }
      },
      {
        path: 'result',
        name: 'Result',
        component: () => import('@/views/result/index.vue'),
        meta: {
          title: '结果页',
          requiresAuth: true
        }
      },
      {
        path: 'exception',
        name: 'Exception',
        component: () => import('@/views/exception/index.vue'),
        meta: {
          title: '异常页',
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

  // 判断该路由是否需要登录权限
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (token) {
      next()
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    next()
  }
})

export default router 