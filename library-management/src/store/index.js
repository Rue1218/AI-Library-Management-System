import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken as saveToken, clearAuth, getUserId, setUserId as saveUserId, getUserRole, setUserRole as saveUserRole } from '@/utils/auth'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userId = ref(getUserId())
  const userInfo = ref(null)
  const role = ref(getUserRole())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === '0' || role.value === 0)
  const isUser = computed(() => role.value === '1' || role.value === 1)

  const setToken = newToken => {
    token.value = newToken
    saveToken(newToken)
  }

  const setUserId = newUserId => {
    userId.value = newUserId
    saveUserId(newUserId)
  }

  const setUserInfo = info => {
    userInfo.value = info
    if (info) {
      saveUserRole(String(info.role))
      role.value = String(info.role)
    }
  }

  const login = async (loginForm) => {
    try {
      const data = await loginApi(loginForm)
      setToken(data.token)
      setUserId(data.userId)
      setUserInfo(data.user)
      return { success: true, data }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  const logout = () => {
    token.value = ''
    userId.value = ''
    userInfo.value = null
    role.value = ''
    clearAuth()
  }

  return {
    token,
    userId,
    userInfo,
    role,
    isLoggedIn,
    isAdmin,
    isUser,
    setToken,
    setUserId,
    setUserInfo,
    login,
    logout
  }
})