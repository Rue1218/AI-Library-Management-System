<template>
  <div class="layout-wrapper">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-glass">
        <div class="header-container">
          <!-- Logo区域 -->
          <div class="logo-section" @click="navigate('/user/dashboard')">
            <div class="logo-icon">
              <svg viewBox="0 0 44 44" fill="none">
                <rect x="4" y="6" width="36" height="32" rx="6" fill="url(#logoGrad)"/>
                <rect x="10" y="2" width="6" height="40" rx="2" fill="#fff" opacity="0.9"/>
                <rect x="20" y="8" width="5" height="28" rx="2" fill="#fff" opacity="0.6"/>
                <rect x="29" y="12" width="5" height="20" rx="2" fill="#fff" opacity="0.4"/>
                <defs>
                  <linearGradient id="logoGrad" x1="4" y1="6" x2="40" y2="38">
                    <stop stop-color="#38BDF8"/>
                    <stop offset="1" stop-color="#0EA5E9"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <span class="logo-text">智典书屋</span>
          </div>
          
          <!-- 主导航菜单 -->
          <nav class="main-nav">
            <div 
              v-for="item in navItems" 
              :key="item.path"
              class="nav-item"
              :class="{ active: isActive(item.path) }"
              @click="navigate(item.path)"
            >
              {{ item.name }}
            </div>
          </nav>
          
          <!-- 搜索框 -->
          <div class="header-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索图书、作者..."
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
              class="search-input glass-input"
              clearable
            />
          </div>
          
          <!-- 用户区域 -->
          <div class="header-right">
            <div class="user-section" v-if="userInfo">
              <el-dropdown trigger="click" @command="handleCommand">
                <div class="user-trigger">
                  <el-avatar v-if="userAvatar" :size="38" :src="userAvatar" class="avatar" />
                  <el-avatar v-else :size="38" class="avatar default">
                    {{ userInfo?.real_name?.charAt(0) || userInfo?.username?.charAt(0) || 'U' }}
                  </el-avatar>
                  <span class="username">{{ userInfo?.real_name || userInfo?.username }}</span>
                  <el-icon class="arrow"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu class="user-dropdown glass-dropdown">
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="settings">
                      <el-icon><Reading /></el-icon>我的借阅
                    </el-dropdown-item>
                    <el-dropdown-item command="reservations">
                      <el-icon><Calendar /></el-icon>我的预约
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <el-button v-else type="primary" class="login-btn" round @click="$router.push('/login')">
              登录
            </el-button>

            <!-- AI 智能助手入口 -->
            <div class="ai-assistant-btn" @click="navigate('/user/ai-chat')" title="AI 智能助手">
              <div class="ai-icon-wrapper">
                <svg viewBox="0 0 24 24" fill="none" width="22" height="22">
                  <defs>
                    <linearGradient id="aiGrad" x1="0" y1="0" x2="24" y2="24">
                      <stop stop-color="#38BDF8"/>
                      <stop offset="1" stop-color="#8B5CF6"/>
                    </linearGradient>
                  </defs>
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" fill="url(#aiGrad)" opacity="0.15"/>
                  <path d="M9 8.5a1 1 0 1 1 2 0 1 1 0 0 1-2 0zm4 0a1 1 0 1 1 2 0 1 1 0 0 1-2 0z" fill="url(#aiGrad)"/>
                  <path d="M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9-4.03-9-9-9zm0 16.5c-4.14 0-7.5-3.36-7.5-7.5S7.86 4.5 12 4.5s7.5 3.36 7.5 7.5-3.36 7.5-7.5 7.5z" fill="url(#aiGrad)"/>
                  <path d="M15.5 11.5h-7c-.28 0-.5.22-.5.5 0 2.21 1.79 4 4 4s4-1.79 4-4c0-.28-.22-.5-.5-.5zm-3.5 3c-1.1 0-2.04-.63-2.5-1.55h5c-.46.92-1.4 1.55-2.5 1.55z" fill="url(#aiGrad)"/>
                  <circle cx="12" cy="1.5" r="1" fill="#8B5CF6" opacity="0.6"/>
                  <circle cx="12" cy="22.5" r="1" fill="#38BDF8" opacity="0.6"/>
                  <circle cx="1.5" cy="12" r="1" fill="#8B5CF6" opacity="0.4"/>
                  <circle cx="22.5" cy="12" r="1" fill="#38BDF8" opacity="0.4"/>
                </svg>
                <span class="ai-pulse"></span>
              </div>
              <span class="ai-label">AI 助手</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 移动端导航 -->
    <div class="mobile-nav">
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        :ellipsis="false"
        @select="handleMobileSelect"
      >
        <el-menu-item index="/user/dashboard">首页</el-menu-item>
        <el-menu-item index="/user/books">检索</el-menu-item>
        <el-menu-item index="/user/borrow">借阅</el-menu-item>
        <el-menu-item index="/user/reserve">预约</el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>
    
    <!-- 底部信息 -->
    <footer class="footer">
      <div class="footer-glass">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">
              <svg viewBox="0 0 44 44" fill="none" width="32" height="32">
                <rect x="4" y="6" width="36" height="32" rx="6" fill="url(#footerLogoGrad)"/>
                <rect x="10" y="2" width="6" height="40" rx="2" fill="#fff" opacity="0.9"/>
                <rect x="20" y="8" width="5" height="28" rx="2" fill="#fff" opacity="0.6"/>
                <rect x="29" y="12" width="5" height="20" rx="2" fill="#fff" opacity="0.4"/>
                <defs>
                  <linearGradient id="footerLogoGrad" x1="4" y1="6" x2="40" y2="38">
                    <stop stop-color="#38BDF8"/>
                    <stop offset="1" stop-color="#0EA5E9"/>
                  </linearGradient>
                </defs>
              </svg>
              <span>智典书屋</span>
            </div>
            <p class="footer-desc">探索知识的海洋，开启智慧之旅</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4>读者服务</h4>
              <a href="#">借阅规则</a>
              <a href="#">预约服务</a>
              <a href="#">续借说明</a>
            </div>
            <div class="link-group">
              <h4>关于我们</h4>
              <a href="#">图书馆简介</a>
              <a href="#">服务时间</a>
              <a href="#">联系方式</a>
            </div>
            <div class="link-group">
              <h4>关注我们</h4>
              <div class="social-icons">
                <span class="social-icon">微</span>
                <span class="social-icon">博</span>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 智典书屋 版权所有</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { clearAuth } from '@/utils/auth'
import { Search, ArrowDown, User, Reading, Calendar, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
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
const userRole = computed(() => userStore.role || localStorage.getItem('role'))

const navItems = computed(() => {
  if (userRole.value === '0') {
    return [
      { name: '控制台', path: '/admin/dashboard' },
      { name: '图书管理', path: '/admin/books' },
      { name: '分类管理', path: '/admin/category' },
      { name: '借阅管理', path: '/admin/borrow' },
      { name: '读者管理', path: '/admin/users' },
      { name: '数据统计', path: '/admin/statistics' },
    ]
  }
  return [
    { name: '首页', path: '/user/dashboard' },
    { name: '馆藏检索', path: '/user/books' },
    { name: '我的借阅', path: '/user/borrow' },
    { name: '我的预约', path: '/user/reserve' },
  ]
})

const isActive = (path) => route.path.includes(path)

const navigate = (path) => router.push(path)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/user/books', query: { keyword: searchKeyword.value } })
  }
}

const handleMobileSelect = (index) => router.push(index)

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
      router.push('/user/profile')
      break
    case 'settings':
      router.push('/user/borrow')
      break
    case 'reservations':
      router.push('/user/reserve')
      break
  }
}
</script>

<style scoped>
.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

/* 顶部导航 - 毛玻璃效果 */
.top-header {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-glass {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.7);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 30px rgba(14, 165, 233, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 72px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 8px rgba(14, 165, 233, 0.3));
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #0EA5E9, #0284C7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 3px;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  padding: 10px 20px;
  color: #64748B;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.25s ease;
  position: relative;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38BDF8, #0EA5E9);
  border-radius: 2px;
  transform: translateX(-50%);
  transition: width 0.25s ease;
}

.nav-item:hover {
  color: #0EA5E9;
  background: rgba(14, 165, 233, 0.08);
}

.nav-item.active {
  color: #0284C7;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15), rgba(14, 165, 233, 0.1));
}

.nav-item.active::after {
  width: 24px;
}

.header-search {
  flex: 1;
  max-width: 380px;
}

.glass-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: inset 0 2px 4px rgba(14, 165, 233, 0.05);
  border: 1px solid rgba(14, 165, 233, 0.15);
  backdrop-filter: blur(10px);
}

.glass-input :deep(.el-input__wrapper:hover),
.glass-input :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(14, 165, 233, 0.4);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.15);
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

/* AI 助手按钮 */
.ai-assistant-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.08), rgba(139, 92, 246, 0.08));
  border: 1px solid rgba(139, 92, 246, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.ai-assistant-btn:hover {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15), rgba(139, 92, 246, 0.15));
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.2);
  transform: translateY(-1px);
}

.ai-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.3), transparent);
  animation: aiPulse 2s ease-in-out infinite;
}

@keyframes aiPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.4); opacity: 0; }
}

.ai-label {
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #38BDF8, #8B5CF6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.25s ease;
  background: rgba(255, 255, 255, 0.5);
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.15);
}

.avatar {
  border: 2px solid rgba(14, 165, 233, 0.3);
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.2);
}

.avatar.default {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
  font-weight: 600;
  border: none;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #0C4A6E;
}

.arrow {
  color: #94A3B8;
  font-size: 12px;
}

.glass-dropdown {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.2);
}

:deep(.user-dropdown .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 10px;
  color: #0C4A6E;
  transition: all 0.2s;
}

:deep(.user-dropdown .el-dropdown-menu__item:hover) {
  background: rgba(14, 165, 233, 0.1);
  color: #0284C7;
}

.login-btn {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
  font-weight: 500;
  padding: 10px 24px;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.3);
}

.login-btn:hover {
  box-shadow: 0 6px 24px rgba(14, 165, 233, 0.4);
  transform: translateY(-1px);
}

/* 移动端导航 */
.mobile-nav {
  display: none;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(14, 165, 233, 0.1);
}

/* 主内容 */
.main-content {
  flex: 1;
}

/* 底部 - 毛玻璃效果 */
.footer {
  margin-top: auto;
}

.footer-glass {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: rgba(12, 74, 110, 0.85);
  padding: 56px 32px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 64px;
}

.footer-brand {
  max-width: 280px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.footer-logo span {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.footer-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  gap: 80px;
}

.link-group h4 {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 18px;
}

.link-group a {
  display: block;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  text-decoration: none;
  margin-bottom: 12px;
  transition: color 0.2s;
}

.link-group a:hover {
  color: #38BDF8;
}

.social-icons {
  display: flex;
  gap: 12px;
}

.social-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s;
}

.social-icon:hover {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  border-color: transparent;
  transform: translateY(-2px);
}

.footer-bottom {
  max-width: 1400px;
  margin: 40px auto 0;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.footer-bottom p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
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
  .main-nav {
    display: none;
  }
  
  .mobile-nav {
    display: block;
  }
  
  .header-search {
    display: none;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .footer-links {
    gap: 40px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
    gap: 16px;
  }
  
  .logo-text {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .footer-links {
    flex-wrap: wrap;
    gap: 32px;
  }
}
</style>
