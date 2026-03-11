<template>
  <div class="admin-layout">
    <!-- 左侧导航栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="sidebar-header">
        <div class="logo-icon" @click="navigate('/admin/dashboard')">
          <svg viewBox="0 0 44 44" fill="none">
            <rect x="4" y="6" width="36" height="32" rx="6" fill="url(#sidebarLogoGrad)"/>
            <rect x="10" y="2" width="6" height="40" rx="2" fill="#fff" opacity="0.9"/>
            <rect x="20" y="8" width="5" height="28" rx="2" fill="#fff" opacity="0.6"/>
            <rect x="29" y="12" width="5" height="20" rx="2" fill="#fff" opacity="0.4"/>
            <defs>
              <linearGradient id="sidebarLogoGrad" x1="4" y1="6" x2="40" y2="38">
                <stop stop-color="#38BDF8"/>
                <stop offset="1" stop-color="#0EA5E9"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span v-if="!isCollapse" class="logo-text">智典书屋</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Reading /></el-icon>
          <template #title>图书管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><FolderOpened /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/borrow">
          <el-icon><Ticket /></el-icon>
          <template #title>借阅管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>读者管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
        <el-menu-item index="/admin/reservations">
          <el-icon><Calendar /></el-icon>
          <template #title>预约管理</template>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer" @click="toggleCollapse">
        <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        <span v-if="!isCollapse">收起</span>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="topbar-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar v-if="userAvatar" :size="36" :src="userAvatar" class="avatar" />
              <el-avatar v-else :size="36" class="avatar default">
                {{ userInfo?.real_name?.charAt(0) || userInfo?.username?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="user-name">{{ userInfo?.real_name || userInfo?.username || '管理员' }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import { clearAuth } from '@/utils/auth'
import { 
  DataAnalysis, Reading, FolderOpened, Ticket, User, TrendCharts, 
  Fold, Expand, ArrowDown, Calendar, Setting, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const checkScreenSize = () => {
  isCollapse.value = window.innerWidth < 1024
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

const userInfo = computed(() => {
  const info = userStore.userInfo
  if (info) return info
  const username = localStorage.getItem('username')
  const userId = localStorage.getItem('userId')
  const realName = localStorage.getItem('realName')
  if (username) {
    return { username, real_name: realName || username, id: userId }
  }
  return null
})

const userAvatar = computed(() => localStorage.getItem('avatar') || '')
const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '控制台',
    '/admin/books': '图书管理',
    '/admin/category': '分类管理',
    '/admin/borrow': '借阅管理',
    '/admin/users': '读者管理',
    '/admin/statistics': '数据统计',
    '/admin/reservations': '预约管理',
    '/admin/profile': '个人中心',
    '/admin/settings': '系统设置'
  }
  return titles[route.path] || '管理后台'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const navigate = (path) => router.push(path)

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        clearAuth()
        router.replace('/login')
      }).catch(() => {})
      break
    case 'profile':
      router.push('/admin/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(14, 165, 233, 0.15);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.1);
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  cursor: pointer;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #0891B2, #0e7490);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item) {
  height: 52px;
  margin: 6px 12px;
  border-radius: 12px;
  color: #64748B;
  font-weight: 500;
}

:deep(.el-menu-item:hover) {
  background: rgba(14, 165, 233, 0.08) !important;
  color: #0891B2;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15), rgba(14, 165, 233, 0.1)) !important;
  color: #0891B2;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #38BDF8, #0EA5E9);
  border-radius: 0 4px 4px 0;
}

:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-menu-item) {
  margin: 6px 8px;
}

.sidebar-footer {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 20px;
  border-top: 1px solid rgba(14, 165, 233, 0.1);
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-footer:hover {
  background: rgba(14, 165, 233, 0.08);
  color: #0891B2;
}

/* 主内容区 */
.main-wrapper {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.3s ease;
}

.sidebar.collapsed + .main-wrapper {
  margin-left: 64px;
}

/* 顶部栏 */
.topbar {
  height: 72px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(14, 165, 233, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #0C4A6E;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 24px;
  transition: all 0.25s;
  background: rgba(255, 255, 255, 0.6);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.15);
}

.avatar {
  border: 2px solid rgba(14, 165, 233, 0.3);
}

.avatar.default {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #0C4A6E;
}

.arrow {
  color: #94A3B8;
  font-size: 12px;
}

.user-dropdown {
  padding: 8px;
}

:deep(.user-dropdown .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: #0C4A6E;
}

:deep(.user-dropdown .el-dropdown-menu__item:hover) {
  background: rgba(14, 165, 233, 0.1);
  color: #0284C7;
}

/* 内容区 */
.main-content {
  flex: 1;
  padding: 24px 32px;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar {
    width: 64px;
  }
  
  .main-wrapper {
    margin-left: 64px;
  }
  
  .logo-text,
  .sidebar-footer span {
    display: none;
  }
}
</style>
