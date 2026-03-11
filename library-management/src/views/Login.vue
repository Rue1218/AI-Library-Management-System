<template>
  <div class="login-page">
    <!-- 背景动画 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>
    
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <svg viewBox="0 0 60 60" fill="none">
              <rect x="4" y="8" width="52" height="44" rx="8" fill="url(#brandGrad)"/>
              <rect x="12" y="4" width="8" height="52" rx="3" fill="#fff" opacity="0.9"/>
              <rect x="26" y="12" width="6" height="36" rx="2" fill="#fff" opacity="0.6"/>
              <rect x="38" y="16" width="6" height="28" rx="2" fill="#fff" opacity="0.4"/>
              <defs>
                <linearGradient id="brandGrad" x1="4" y1="8" x2="56" y2="52">
                  <stop stop-color="#38BDF8"/>
                  <stop offset="1" stop-color="#0EA5E9"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="brand-title">智典书屋</h1>
          <p class="brand-desc">探索知识的海洋，开启智慧之旅</p>
          
          <div class="brand-features">
            <div class="feature-item">
              <el-icon :size="24"><Collection /></el-icon>
              <span>海量藏书</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><Clock /></el-icon>
              <span>随时借阅</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><Calendar /></el-icon>
              <span>在线预约</span>
            </div>
          </div>
        </div>
        
        <div class="brand-quote">
          <p>"阅读是人类进步的阶梯"</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-glass">
          <template v-if="isLogin">
            <div class="form-header">
              <h2 class="form-title">欢迎回来</h2>
              <p class="form-subtitle">登录您的账户</p>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <input 
                  type="text" 
                  v-model="loginForm.username" 
                  placeholder="请输入账号"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  v-model="loginForm.password" 
                  placeholder="请输入密码"
                  class="form-input"
                />
                <span class="password-toggle" @click="showPassword = !showPassword">
                  <el-icon v-if="showPassword"><View /></el-icon>
                  <el-icon v-else><Hide /></el-icon>
                </span>
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><UserFilled /></el-icon>
                <select v-model="loginForm.role" class="form-input role-select">
                  <option value="">请选择角色</option>
                  <option value="0">管理员</option>
                  <option value="1">普通用户</option>
                </select>
              </div>
            </div>

            <button 
              type="button" 
              class="submit-btn"
              @click="handleLogin"
              :disabled="loading"
            >
              <span v-if="!loading">登 录</span>
              <span v-else class="loading-text">登录中...</span>
            </button>

            <div class="switch-mode">
              还没有账号？<a href="javascript:;" @click="switchMode">立即注册</a>
            </div>
          </template>

          <template v-else>
            <div class="form-header">
              <h2 class="form-title">用户注册</h2>
              <p class="form-subtitle">创建您的账户</p>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <input 
                  type="text" 
                  v-model="registerForm.username" 
                  placeholder="请输入账号 (3-20个字符)"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <input 
                  type="text" 
                  v-model="registerForm.realName" 
                  placeholder="请输入真实姓名"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  v-model="registerForm.password" 
                  placeholder="请输入密码 (至少6位)"
                  class="form-input"
                />
                <span class="password-toggle" @click="showPassword = !showPassword">
                  <el-icon v-if="showPassword"><View /></el-icon>
                  <el-icon v-else><Hide /></el-icon>
                </span>
              </div>
            </div>

            <div class="form-group">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <input 
                  :type="showConfirmPassword ? 'text' : 'password'" 
                  v-model="registerForm.confirmPassword" 
                  placeholder="请确认密码"
                  class="form-input"
                />
                <span class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                  <el-icon v-if="showConfirmPassword"><View /></el-icon>
                  <el-icon v-else><Hide /></el-icon>
                </span>
              </div>
            </div>

            <button 
              type="button" 
              class="submit-btn"
              @click="handleRegister"
              :disabled="loading"
            >
              <span v-if="!loading">注 册</span>
              <span v-else class="loading-text">注册中...</span>
            </button>

            <div class="switch-mode">
              已有账号？<a href="javascript:;" @click="switchMode">立即登录</a>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { useUserStore } from '@/store'
import { User, Lock, View, Hide, UserFilled, Collection, Clock, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: '1'
})

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: ''
})

const switchMode = () => {
  isLogin.value = !isLogin.value
  showPassword.value = false
  showConfirmPassword.value = false
}

const handleLogin = async () => {
  if (!loginForm.username) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!loginForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!loginForm.role) {
    ElMessage.warning('请选择角色')
    return
  }

  loading.value = true
  try {
    const result = await userStore.login(loginForm)
    if (result.success && result.data?.token) {
      const { data } = result
      ElMessage.success('登录成功')
      localStorage.setItem('username', data.user.username)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('realName', data.user.realName)
      localStorage.setItem('avatar', data.user.avatar || '')
      
      const path = loginForm.role === '0' ? '/admin/dashboard' : '/user/dashboard'
      router.replace(path)
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (err) {
    console.error('登录错误:', err)
    ElMessage.error('登录出错')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    ElMessage.warning('账号长度需3-20个字符')
    return
  }
  if (!registerForm.realName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!registerForm.password || registerForm.password.length < 6) {
    ElMessage.warning('密码长度需至少6位')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      realName: registerForm.realName,
      role: 1
    })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
  } catch (err) {
    console.error('注册错误:', err)
    ElMessage.error('注册出错')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0EA5E9 0%, #0284C7 50%, #0369A1 100%);
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: float 15s ease-in-out infinite;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: #38BDF8;
  top: -200px;
  left: -100px;
  opacity: 0.4;
}

.shape-2 {
  width: 500px;
  height: 500px;
  background: #7DD3FC;
  bottom: -150px;
  right: -100px;
  animation-delay: -5s;
}

.shape-3 {
  width: 400px;
  height: 400px;
  background: #0EA5E9;
  top: 40%;
  left: 30%;
  animation-delay: -10s;
}

.shape-4 {
  width: 300px;
  height: 300px;
  background: #BAE6FD;
  bottom: 20%;
  left: 10%;
  animation-delay: -7s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1) rotate(0deg); }
  25% { transform: translate(30px, -40px) scale(1.05) rotate(5deg); }
  50% { transform: translate(-20px, 20px) scale(0.95) rotate(-5deg); }
  75% { transform: translate(40px, 30px) scale(1.02) rotate(3deg); }
}

/* 登录容器 */
.login-container {
  position: relative;
  display: flex;
  max-width: 1000px;
  width: 90%;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
}

/* 左侧品牌区 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.9), rgba(2, 132, 199, 0.95));
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

.brand-content {
  text-align: center;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.brand-logo svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 20px rgba(0, 0, 0, 0.2));
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: 4px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.feature-item .el-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-item span {
  font-size: 13px;
}

.brand-quote {
  text-align: center;
}

.brand-quote p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  font-style: italic;
}

/* 右侧表单区 */
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: rgba(255, 255, 255, 0.95);
}

.login-glass {
  width: 100%;
  max-width: 380px;
}

.form-header {
  text-align: center;
  margin-bottom: 36px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #64748B;
}

.form-group {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #F8FAFC;
  border: 2px solid #E2E8F0;
  border-radius: 14px;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #0EA5E9;
  background: #fff;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.15);
}

.input-icon {
  width: 50px;
  color: #94A3B8;
  font-size: 18px;
}

.form-input {
  flex: 1;
  height: 50px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #1E293B;
  outline: none;
}

.form-input::placeholder {
  color: #94A3B8;
}

.role-select {
  cursor: pointer;
}

.role-select option {
  color: #1E293B;
}

.password-toggle {
  width: 50px;
  height: 50px;
  cursor: pointer;
  color: #94A3B8;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: #0EA5E9;
}

.submit-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.4);
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.5);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-text {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.switch-mode {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #64748B;
}

.switch-mode a {
  color: #0EA5E9;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.switch-mode a:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    min-height: auto;
  }
  
  .login-brand {
    padding: 40px 24px;
  }
  
  .brand-features {
    gap: 16px;
  }
  
  .feature-item .el-icon {
    width: 40px;
    height: 40px;
  }
  
  .login-form-wrapper {
    padding: 32px 24px;
  }
}

/* 禁用Edge密码眼睛图标 */
:deep(input::-ms-reveal) {
  display: none !important;
}
:deep(input::-ms-clear) {
  display: none !important;
}
</style>
