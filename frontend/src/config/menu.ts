import {
  House,
  List,
  Document,
  Plus,
  User,
  Grid,
  Folder,
  UserFilled,
  PriceTag
} from '@element-plus/icons-vue'

export const menuItems = [
  {
    title: 'Dashboard',
    icon: House,
    path: '/dashboard'
  },
  {
    title: 'Gantt Chart',
    icon: Document,
    path: '/gantt'
  },
  {
    title: 'Task Board',
    icon: Grid,
    path: '/kanban'
  },
  {
    title: 'Create Project',
    icon: Plus,
    path: '/project/create',
    roles: ['1'] // 只有 leader 可见
  },
  {
    title: 'Project List',
    icon: Folder,
    path: '/project/list',
    roles: ['1', '0'] // 1: leader, 0: member
  },
  {
    title: 'Create Task',
    icon: Plus,
    path: '/task/create',
    roles: ['1'] // 只有 leader 可见
  },
  {
    title: 'Task List',
    icon: List,
    path: '/list',
    roles: ['1', '0'] // 1: leader, 0: member
  },
  {
    title: '创建标签',
    icon: Plus,
    path: '/tag/create',
    roles: ['1'] // 只有 leader 可见
  },
  {
    title: '标签列表',
    icon: PriceTag,
    path: '/tag/list',
    roles: ['1', '0'] // 1: leader, 0: member
  },
  
  {
    title: '员工管理',
    icon: UserFilled,
    path: '/employee',
    roles: ['2'] // 只有 admin 可见
  },
  {
    title: 'Personal Center',
    icon: User,
    path: '/profile'
  }
] 