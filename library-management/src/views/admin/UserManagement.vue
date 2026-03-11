<template>
  <div class="page-container">
    <div class="bg-blob">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-header glass">
      <div class="header-left">
        <h2 class="page-title">读者管理</h2>
        <p class="page-desc">管理注册读者信息</p>
      </div>
      <el-button class="add-btn" type="primary" :icon="Plus" @click="handleAdd">
        新增读者
      </el-button>
    </div>

    <div class="search-card glass">
      <div class="search-input-wrapper">
        <el-icon class="search-icon"><Search /></el-icon>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索账号/姓名/手机号"
          clearable
          @clear="fetchUsers"
          @keyup.enter="fetchUsers"
        />
      </div>
      <el-button class="search-btn" type="primary" @click="fetchUsers">查询</el-button>
    </div>

    <div class="table-card glass">
      <el-table :data="tableData" style="width: 100%" stripe v-loading="loading">
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <div class="user-avatar">
              <el-avatar v-if="scope.row.avatar" :src="scope.row.avatar" :size="44" />
              <el-avatar v-else :size="44">
                {{ scope.row.realName?.charAt(0) || scope.row.username?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-badge">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="realName" label="姓名" width="130">
          <template #default="{ row }">
            <span class="name-text">{{ row.realName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" class="status-tag active">正常</el-tag>
            <el-tag v-else class="status-tag disabled">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      @close="resetForm"
      class="user-dialog"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="user-header">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar-preview" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="36"><User /></el-icon>
              </div>
              <div v-if="userForm.avatar" class="avatar-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <div class="avatar-actions" v-if="userForm.avatar">
              <el-button type="danger" size="small" circle @click="handleRemoveAvatar">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <span class="avatar-tip">点击更换头像</span>
          </div>
        </div>
        
        <el-form :model="userForm" :rules="formRules" ref="formRef" label-position="top">
          <div class="form-row">
            <el-form-item label="账号" prop="username">
              <el-input v-model="userForm.username" placeholder="请输入账号" :disabled="isEdit">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="userForm.realName" placeholder="请输入姓名">
                <template #prefix>
                  <el-icon><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>
          
          <el-form-item v-if="!isEdit" label="密码" prop="password">
            <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="账号状态" prop="status">
            <el-radio-group v-model="userForm.status" class="status-radio-group">
              <el-radio :value="1" class="status-radio active">
                <el-icon><CircleCheck /></el-icon>
                <span>正常</span>
              </el-radio>
              <el-radio :value="0" class="status-radio disabled">
                <el-icon><CircleClose /></el-icon>
                <span>禁用</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, UserFilled, Lock, Phone, Message, Camera, Delete, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getUsers, createUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import { uploadImage, deleteImage } from '@/api/upload'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  status: 1,
  avatar: ''
})

const pendingAvatarFile = ref(null)
const originalAvatar = ref('')

const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    let users = (res.data || res).filter(item => item.role === 1)
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      users = users.filter(item => 
        item.username?.toLowerCase().includes(keyword) ||
        item.realName?.toLowerCase().includes(keyword) ||
        item.phone?.includes(keyword)
      )
    }
    
    tableData.value = users.sort((a, b) => a.id - b.id)
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增读者'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑读者'
  Object.assign(userForm, row)
  originalAvatar.value = row.avatar
  userForm.password = ''
  pendingAvatarFile.value = null
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该读者吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchUsers()
  }).catch(() => {})
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}用户 "${row.realName || row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const newStatus = row.status === 1 ? 0 : 1
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  }).catch(() => {})
}

const handleRemoveAvatar = async () => {
  if (isEdit.value && originalAvatar.value && userForm.avatar === originalAvatar.value) {
    try {
      await deleteImage(originalAvatar.value)
    } catch (e) {
      console.error('删除图片失败', e)
    }
  }
  userForm.avatar = ''
  pendingAvatarFile.value = null
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
    
    let avatarUrl = userForm.avatar
    
    if (pendingAvatarFile.value) {
      const res = await uploadImage(pendingAvatarFile.value, 'user')
      if (res) {
        avatarUrl = res
      }
    }
    
    const data = { 
      ...userForm,
      avatar: avatarUrl
    }
    
    if (isEdit.value) {
      delete data.password
      delete data.username
      
      if (userForm.avatar !== originalAvatar.value && originalAvatar.value) {
        try {
          await deleteImage(originalAvatar.value)
        } catch (e) {
          console.error('删除旧图片失败', e)
        }
      }
      
      await updateUser(data.id, data)
      ElMessage.success('更新成功')
    } else {
      await createUser(data)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const resetForm = () => {
  userForm.id = null
  userForm.username = ''
  userForm.realName = ''
  userForm.phone = ''
  userForm.email = ''
  userForm.password = ''
  userForm.status = 1
  userForm.avatar = ''
  pendingAvatarFile.value = null
  originalAvatar.value = ''
}

onMounted(() => {
  fetchUsers()
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
  opacity: 0.4;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #10B981, #059669);
  top: -150px;
  left: -100px;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #60A5FA, #3B82F6);
  bottom: -100px;
  right: -100px;
}

.glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-radius: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E3A8A;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #64748B;
}

.add-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

.search-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-radius: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.search-input-wrapper {
  flex: 1;
  max-width: 400px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  z-index: 1;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: none;
}

.search-btn {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border: none;
  border-radius: 12px;
  padding: 10px 20px;
}

.table-card {
  padding: 24px;
  border-radius: 20px;
  position: relative;
  z-index: 1;
}

.table-card :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(59, 130, 246, 0.05);
  --el-table-row-hover-bg-color: rgba(59, 130, 246, 0.05);
  --el-table-border-color: rgba(59, 130, 246, 0.1);
}

.table-card :deep(.el-table th.el-table__cell) {
  background: rgba(59, 130, 246, 0.08);
  color: #1E3A8A;
  font-weight: 600;
}

.table-card :deep(.el-table td.el-table__cell) {
  padding: 14px 0;
}

.user-avatar {
  border-radius: 50%;
  overflow: hidden;
}

.id-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
}

.name-text {
  font-weight: 600;
  color: #1E3A8A;
}

.status-tag {
  border-radius: 8px;
  font-weight: 500;
}

.status-tag.active {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border: none;
}

.status-tag.disabled {
  background: rgba(239, 68, 68, 0.1);
  color: #DC2626;
  border: none;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 2px dashed rgba(59, 130, 246, 0.3);
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
}

.avatar-uploader:hover {
  border-color: #3B82F6;
  background: rgba(59, 130, 246, 0.05);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #94A3B8;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 8px;
}

.glass-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(30px) !important;
  -webkit-backdrop-filter: blur(30px) !important;
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(59, 130, 246, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.4) !important;
  overflow: hidden;
}

.glass-dialog :deep(.el-dialog::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.08) 0%, rgba(167, 139, 250, 0.05) 100%);
  pointer-events: none;
}

.glass-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  padding: 24px 28px;
  position: relative;
  margin-right: 0;
}

.glass-dialog :deep(.el-dialog__title) {
  color: #1E3A8A;
  font-weight: 700;
  font-size: 18px;
}

.glass-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.glass-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748B;
  font-size: 18px;
  transition: all 0.2s;
}

.glass-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #8B5CF6;
  transform: rotate(90deg);
}

.glass-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.glass-dialog :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 14px;
}

.glass-dialog :deep(.el-input__wrapper),
.glass-dialog :deep(.el-textarea__inner),
.glass-dialog :deep(.el-select .el-input__wrapper),
.glass-dialog :deep(.el-radio-group) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.glass-dialog :deep(.el-input__wrapper:hover),
.glass-dialog :deep(.el-textarea__inner:hover),
.glass-dialog :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(59, 130, 246, 0.4) !important;
}

.glass-dialog :deep(.el-input__wrapper.is-focus),
.glass-dialog :deep(.el-textarea__inner:focus),
.glass-dialog :deep(.el-select .el-input.is-focus) {
  border-color: #8B5CF6 !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1) !important;
}

.glass-dialog :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #8B5CF6;
  border-color: #8B5CF6;
}

.glass-dialog :deep(.el-radio__label) {
  color: #1E3A8A;
}

.glass-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid rgba(59, 130, 246, 0.1);
  padding: 20px 28px;
}

.glass-dialog :deep(.el-button) {
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.glass-dialog :deep(.el-button--primary) {
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  border: none;
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.3);
}

.glass-dialog :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
}

.glass-dialog :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #8B5CF6;
}

.glass-dialog :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(139, 92, 246, 0.05);
  border-color: rgba(139, 92, 246, 0.3);
}

/* 用户对话框新样式 */
.user-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 
    0 25px 60px -12px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(139, 92, 246, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.5) !important;
  overflow: hidden;
}

.user-dialog :deep(.el-dialog__header) {
  padding: 24px 28px;
  margin: 0;
  border-bottom: 1px solid rgba(139, 92, 246, 0.1);
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.05) 0%, rgba(167, 139, 250, 0.03) 100%);
}

.user-dialog :deep(.el-dialog__title) {
  color: #1E3A8A;
  font-weight: 700;
  font-size: 20px;
}

.user-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.user-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748B;
  font-size: 18px;
  transition: all 0.3s;
}

.user-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #8B5CF6;
  transform: rotate(90deg);
}

.user-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-content {
  padding: 28px;
}

.user-header {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-dialog .avatar-uploader {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 3px solid rgba(139, 92, 246, 0.2);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1) 0%, rgba(167, 139, 250, 0.05) 100%);
}

.user-dialog .avatar-uploader:hover {
  border-color: #8B5CF6;
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.2);
}

.user-dialog .avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-dialog .avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B5CF6;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1) 0%, rgba(167, 139, 250, 0.05) 100%);
}

.user-dialog .avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.user-dialog .avatar-overlay .el-icon {
  font-size: 24px;
  color: #fff;
}

.user-dialog .avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.user-dialog .avatar-actions {
  margin-top: 8px;
}

.user-dialog .avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94A3B8;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.user-dialog :deep(.el-form-item__label) {
  color: #1E3A8A;
  font-weight: 600;
  font-size: 13px;
  padding: 0 0 6px 0;
  line-height: 1;
}

.user-dialog :deep(.el-input__wrapper),
.user-dialog :deep(.el-textarea__inner),
.user-dialog :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(139, 92, 246, 0.2) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.user-dialog :deep(.el-input__wrapper:hover),
.user-dialog :deep(.el-textarea__inner:hover),
.user-dialog :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(139, 92, 246, 0.4) !important;
}

.user-dialog :deep(.el-input__wrapper.is-focus),
.user-dialog :deep(.el-textarea__inner:focus),
.user-dialog :deep(.el-select .el-input.is-focus) {
  border-color: #8B5CF6 !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1) !important;
}

.user-dialog :deep(.el-input__prefix) {
  color: #8B5CF6;
}

.status-radio-group {
  display: flex;
  gap: 16px;
}

.status-radio {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.status-radio .el-icon {
  font-size: 18px;
}

.status-radio.active {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
  color: #10B981;
}

.status-radio.active:hover {
  border-color: #10B981;
}

.status-radio.disabled {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #EF4444;
}

.status-radio.disabled:hover {
  border-color: #EF4444;
}

.user-dialog :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: inherit;
}

.user-dialog :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: currentColor;
  border-color: currentColor;
}

.user-dialog :deep(.el-dialog__footer) {
  padding: 20px 28px;
  border-top: 1px solid rgba(139, 92, 246, 0.1);
  background: rgba(139, 92, 246, 0.02);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  border: none;
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
}

.dialog-footer .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(139, 92, 246, 0.2);
  color: #8B5CF6;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  background: rgba(139, 92, 246, 0.05);
  border-color: rgba(139, 92, 246, 0.3);
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
