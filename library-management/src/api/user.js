import request from '@/utils/request'

export const getUsers = () => {
  return request({
    url: '/users',
    method: 'get'
  })
}

export const getUserInfo = () => {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

export const updateUserProfile = (data) => {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

export const updateUserPassword = (password) => {
  return request({
    url: '/users/password',
    method: 'put',
    data: { password }
  })
}

export const createUser = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export const updateUser = (id, data) => {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}