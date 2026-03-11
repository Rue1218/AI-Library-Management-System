import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAuth, isTokenExpired } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  // 管理员端布局
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/AdminLayout.vue'),
    meta: { requiresAuth: true, hidden: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', role: '0' }
      },
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/BookManagement.vue'),
        meta: { title: '图书管理', role: '0' }
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: { title: '分类管理', role: '0' }
      },
      {
        path: 'borrow',
        name: 'AdminBorrow',
        component: () => import('@/views/admin/BorrowManagement.vue'),
        meta: { title: '借阅管理', role: '0' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '读者管理', role: '0' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '数据统计', role: '0' }
      },
      {
        path: 'reservations',
        name: 'AdminReservations',
        component: () => import('@/views/admin/ReservationManagement.vue'),
        meta: { title: '预约管理', role: '0' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人中心', role: '0' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置', role: '0' }
      }
    ]
  },
  // 用户端布局
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true, hidden: true },
    children: [
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页', role: '1' }
      },
      {
        path: 'books',
        name: 'UserBooks',
        component: () => import('@/views/user/Books.vue'),
        meta: { title: '馆藏检索', role: '1' }
      },
      {
        path: 'borrow',
        name: 'UserBorrow',
        component: () => import('@/views/user/Borrow.vue'),
        meta: { title: '我的借阅', role: '1' }
      },
      {
        path: 'reserve',
        name: 'UserReserve',
        component: () => import('@/views/user/Reserve.vue'),
        meta: { title: '预约管理', role: '1' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', role: '1' }
      },
      {
        path: 'book/:id',
        name: 'BookDetail',
        component: () => import('@/views/user/BookDetail.vue'),
        meta: { title: '图书详情', role: '1' }
      },
      {
        path: 'resources/:type',
        name: 'DigitalResources',
        component: () => import('@/views/user/DigitalResources.vue'),
        meta: { title: '数字资源', role: '1' }
      },
      {
        path: 'ai-chat',
        name: 'UserAiChat',
        component: () => import('@/views/user/AiAssistant.vue'),
        meta: { title: 'AI 智能助手', role: '1' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = import.meta.env.VITE_APP_TITLE + (to.meta.title ? ` - ${to.meta.title}` : '')

  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const hasValidToken = !!token && !isTokenExpired(token)

  const isAdmin = role === '0'
  const isUser = role === '1'
  const isValidRole = isAdmin || isUser

  if (to.path === '/') {
    if (!hasValidToken || !isValidRole) {
      if (token && !hasValidToken) {
        clearAuth()
      }
      next('/login')
    } else {
      next('/' + (isAdmin ? 'admin' : 'user') + '/dashboard')
    }
    return
  }

  if (to.path === '/login') {
    if (token && !hasValidToken) {
      clearAuth()
      next()
      return
    }

    if (hasValidToken && isValidRole) {
      next('/' + (isAdmin ? 'admin' : 'user') + '/dashboard')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (!hasValidToken) {
    ElMessage.warning('登录已过期，请重新登录')
    clearAuth()
    next('/login')
    return
  }

  if (!isValidRole) {
    ElMessage.warning('用户角色无效，请重新登录')
    clearAuth()
    next('/login')
    return
  }

  const requiredRole = to.meta.role || (to.matched.length > 0 ? to.matched[0].meta.role : null)
  if (requiredRole && role !== requiredRole) {
    ElMessage.warning('无权访问该页面')
    next('/' + (isAdmin ? 'admin' : 'user') + '/dashboard')
    return
  }

  next()
})

export default router
