import {
  House,
  List,
  Document,
  Plus,
  User,
  Grid,
  Folder
} from '@element-plus/icons-vue'

export const menuItems = [
  {
    title: '仪表盘',
    icon: House,
    path: '/dashboard'
  },
  {
    title: '甘特图',
    icon: Document,
    path: '/gantt'
  },
  {
    title: '任务看板',
    icon: Grid,
    path: '/kanban'
  },
  {
    title: '新增项目',
    icon: Plus,
    path: '/project/create',
    roles: ['1'] // 只有 leader 可见
  },
  {
    title: '项目列表',
    icon: Folder,
    path: '/project/list',
    roles: ['1', '2'] // 1: leader, 2: member
  },
  {
    title: '新增任务',
    icon: Plus,
    path: '/task/create',
    roles: ['1'] // 只有 leader 可见
  },
  {
    title: '任务列表',
    icon: List,
    path: '/list'
  },
  {
    title: '个人中心',
    icon: User,
    path: '/profile'
  }
] 