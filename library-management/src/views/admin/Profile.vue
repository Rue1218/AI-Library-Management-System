<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="profile-header glass">
      <div class="header-content">
        <h2 class="page-title">个人中心</h2>
        <p class="page-desc">管理您的账户信息</p>
      </div>
    </div>

    <div class="profile-content">
      <div class="profile-card glass">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar-preview" />
              <el-icon v-else class="avatar-uploader-icon"><User /></el-icon>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
          </div>
          <span class="avatar-tip">点击更换头像</span>
        </div>

        <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="100px" class="profile-form">
          <el-form-item label="账号">
            <el-input v-model="userForm.username" disabled class="disabled-input" />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="userForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-divider>
            <span class="divider-text">安全设置</span>
          </el-divider>
          
          <el-form-item label="修改密码">
            <el-switch v-model="changePassword" class="password-switch" />
          </el-form-item>
          
          <template v-if="changePassword">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="userForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="userForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
          </template>
          
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleSubmit">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Camera } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'
import { getUserInfo, updateUserProfile, updateUserPassword } from '@/api/user'

const formRef = ref(null)
const changePassword = ref(false)
const pendingAvatarFile = ref(null)

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
    await formRef.value.validate()
    
    if (pendingAvatarFile.value) {
      const res = await uploadImage(pendingAvatarFile.value, 'user')
      if (res) {
        userForm.avatar = res
      }
    }
    
    if (changePassword.value) {
      if (!userForm.newPassword) {
        ElMessage.warning('请输入新密码')
        return
      }
      if (userForm.newPassword !== userForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
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
    if (e !== 'cancel') {
      ElMessage.error('保存失败')
    }
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

.bg-blob {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.35;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  top: -150px;
  left: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #8B5CF6, #A78BFA);
  top: 30%;
  right: -100px;
}

.blob-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #10B981, #34D399);
  bottom: -100px;
  left: 30%;
}

.glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.profile-header {
  padding: 32px;
  border-radius: 24px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1E3A8A;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #64748B;
}

.profile-content {
  max-width: 700px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.profile-card {
  padding: 40px;
  border-radius: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.avatar-wrapper {
  position: relative;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  border: 4px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
  transition: all 0.3s ease;
  position: relative;
}

.avatar-uploader:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.3);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 48px;
  color: #3B82F6;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(59, 130, 246, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.avatar-overlay .el-icon {
  font-size: 28px;
  color: #fff;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #64748B;
}

.profile-form :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 14px;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.profile-form :deep(.el-input__wrapper:hover),
.profile-form :deep(.el-input__wrapper.is-focus) {
  border-color: #3B82F6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.profile-form :deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border-color: #3B82F6;
}

.disabled-input :deep(.el-input__wrapper) {
  background: #F1F5F9 !important;
  border-color: #E2E8F0 !important;
}

.divider-text {
  color: #64748B;
  font-size: 13px;
  font-weight: 500;
}

.password-switch :deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border-color: #3B82F6;
}

.submit-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  padding: 12px 32px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

:deep(.el-divider) {
  border-color: rgba(59, 130, 246, 0.1);
  margin: 32px 0;
}
</style>
