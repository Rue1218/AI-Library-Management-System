<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-desc">管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-content">
      <!-- 左侧头像卡片 -->
      <div class="avatar-card glass-card">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
          >
            <div class="avatar-wrapper">
              <el-avatar v-if="userForm.avatar" :size="140" :src="userForm.avatar" class="avatar-img" />
              <el-avatar v-else :size="140" class="avatar-img default">
                {{ userForm.realName?.charAt(0) || userForm.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="28"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
          <div class="user-name">{{ userForm.realName || userForm.username }}</div>
          <div class="user-role">普通读者</div>
        </div>
        
        <div class="avatar-stats">
          <div class="stat-item">
            <span class="stat-num">{{ borrowCount }}</span>
            <span class="stat-text">借阅中</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ reserveCount }}</span>
            <span class="stat-text">预约中</span>
          </div>
        </div>
      </div>

      <!-- 右侧信息表单 -->
      <div class="info-card glass-card">
        <div class="card-header">
          <h3 class="card-title">基本信息</h3>
        </div>
        
        <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="100px" class="profile-form">
          <el-form-item label="账号" class="form-item">
            <el-input v-model="userForm.username" disabled class="form-input disabled" />
          </el-form-item>
          
          <el-form-item label="姓名" prop="realName" class="form-item">
            <el-input v-model="userForm.realName" placeholder="请输入姓名" class="form-input" />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone" class="form-item">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" class="form-input" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email" class="form-item">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" class="form-input" />
          </el-form-item>
        </el-form>

        <el-divider class="custom-divider" />

        <div class="card-header">
          <h3 class="card-title">安全设置</h3>
        </div>

        <div class="password-section">
          <div class="password-toggle">
            <span class="toggle-label">修改密码</span>
            <el-switch v-model="changePassword" class="toggle-switch" />
          </div>
          
          <transition name="fade-slide">
            <div v-if="changePassword" class="password-form">
              <el-form-item label="新密码" prop="newPassword" class="form-item">
                <el-input v-model="userForm.newPassword" type="password" placeholder="请输入新密码" show-password class="form-input" />
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
                <el-input v-model="userForm.confirmPassword" type="password" placeholder="请确认新密码" show-password class="form-input" />
              </el-form-item>
            </div>
          </transition>
        </div>

        <div class="form-actions">
          <el-button type="primary" size="large" @click="handleSubmit" class="submit-btn">
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Check } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'
import { getUserInfo, updateUserProfile, updateUserPassword } from '@/api/user'
import { getUserStats } from '@/api/borrow'
import { getReserveCount } from '@/api/reservation'

const formRef = ref(null)
const changePassword = ref(false)
const pendingAvatarFile = ref(null)
const borrowCount = ref(0)
const reserveCount = ref(0)

const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  newPassword: [{ min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    Object.assign(userForm, res)
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }
}

const fetchStats = async () => {
  try {
    const stats = await getUserStats()
    borrowCount.value = stats.borrowingCount || 0
  } catch (e) {
    console.error('获取借阅统计失败', e)
  }
  
  try {
    const count = await getReserveCount()
    reserveCount.value = count || 0
  } catch (e) {
    console.error('获取预约统计失败', e)
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  pendingAvatarFile.value = file
  userForm.avatar = URL.createObjectURL(file)
  return false
}

const handleSubmit = async () => {
  try {
    if (changePassword.value) {
      if (!userForm.newPassword) {
        ElMessage.warning('请输入新密码')
        return
      }
      if (userForm.newPassword !== userForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
    }
    
    if (pendingAvatarFile.value) {
      const res = await uploadImage(pendingAvatarFile.value, 'user')
      if (res) {
        userForm.avatar = res
      }
    }
    
    if (changePassword.value) {
      await updateUserPassword(userForm.newPassword)
    }
    
    await updateUserProfile({
      id: userForm.id,
      realName: userForm.realName,
      phone: userForm.phone,
      email: userForm.email,
      avatar: userForm.avatar
    })
    
    localStorage.setItem('realName', userForm.realName)
    localStorage.setItem('avatar', userForm.avatar)
    
    ElMessage.success('保存成功')
    changePassword.value = false
    userForm.newPassword = ''
    userForm.confirmPassword = ''
    pendingAvatarFile.value = null
    
    setTimeout(() => {
      window.location.reload()
    }, 500)
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 32px 24px;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%);
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  color: #0369A1;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 15px;
  color: #0369A1;
  opacity: 0.7;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 28px;
}

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.1);
}

/* 头像卡片 */
.avatar-card {
  text-align: center;
}

.avatar-section {
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.15);
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-img {
  border: 4px solid rgba(14, 165, 233, 0.3);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.2);
}

.avatar-img.default {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
  color: #fff;
  font-size: 48px;
  font-weight: 600;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(14, 165, 233, 0.85);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay span {
  font-size: 12px;
  margin-top: 4px;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  color: #0C4A6E;
  margin-top: 20px;
  margin-bottom: 6px;
}

.user-role {
  font-size: 14px;
  color: #0EA5E9;
  background: rgba(14, 165, 233, 0.1);
  padding: 6px 16px;
  border-radius: 20px;
  display: inline-block;
}

.avatar-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: #0EA5E9;
}

.stat-text {
  font-size: 13px;
  color: #64748B;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(14, 165, 233, 0.2);
}

/* 信息卡片 */
.info-card {
  padding: 32px 40px;
}

.card-header {
  margin-bottom: 28px;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #0369A1;
}

.profile-form {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 24px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  box-shadow: none;
}

.form-input :deep(.el-input__wrapper:hover),
.form-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(14, 165, 233, 0.5);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.15);
}

.form-input.disabled :deep(.el-input__wrapper) {
  background: rgba(14, 165, 233, 0.08);
  border-color: rgba(14, 165, 233, 0.1);
}

.custom-divider {
  margin: 28px 0;
  border-color: rgba(14, 165, 233, 0.15);
}

/* 密码设置 */
.password-section {
  margin-bottom: 32px;
}

.password-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(14, 165, 233, 0.05);
  border-radius: 14px;
  margin-bottom: 16px;
}

.toggle-label {
  font-size: 15px;
  font-weight: 600;
  color: #0C4A6E;
}

.toggle-switch :deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9);
}

.password-form {
  padding: 20px;
  background: rgba(14, 165, 233, 0.05);
  border-radius: 14px;
}

/* 表单按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
}

.submit-btn {
  background: linear-gradient(135deg, #38BDF8, #0EA5E9) !important;
  border: none !important;
  padding: 14px 36px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.45);
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 900px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .avatar-card {
    text-align: center;
  }
  
  .avatar-stats {
    justify-content: center;
  }
}
</style>
