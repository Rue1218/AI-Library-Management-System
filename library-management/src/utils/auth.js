export const getToken = () => {
  return localStorage.getItem('token')
}

const AUTH_STORAGE_KEYS = ['token', 'userId', 'role', 'username', 'realName', 'avatar']

export const setToken = token => {
  return localStorage.setItem('token', token)
}

export const removeToken = () => {
  return localStorage.removeItem('token')
}

export const getUserId = () => {
  return localStorage.getItem('userId')
}

export const setUserId = userId => {
  return localStorage.setItem('userId', userId)
}

export const removeUserId = () => {
  return localStorage.removeItem('userId')
}

export const getUserRole = () => {
  return localStorage.getItem('role')
}

export const setUserRole = role => {
  return localStorage.setItem('role', role)
}

export const removeUserRole = () => {
  return localStorage.removeItem('role')
}

export const clearAuth = () => {
  AUTH_STORAGE_KEYS.forEach(key => localStorage.removeItem(key))
}

export const isTokenExpired = (token = getToken()) => {
  if (!token) return true

  try {
    const payload = token.split('.')[1]
    if (!payload) return true

    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(normalized.length + (4 - normalized.length % 4) % 4, '=')
    const decoded = JSON.parse(window.atob(padded))

    if (!decoded.exp) return false
    return decoded.exp * 1000 <= Date.now()
  } catch (error) {
    return true
  }
}
