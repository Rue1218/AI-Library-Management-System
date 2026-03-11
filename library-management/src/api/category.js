import request from '@/utils/request'

export const getCategories = () => {
  return request({
    url: '/api/category/list',
    method: 'get'
  })
}

export const getCategory = (id) => {
  return request({
    url: `/api/category/${id}`,
    method: 'get'
  })
}

export const addCategory = (data) => {
  return request({
    url: '/api/category',
    method: 'post',
    data
  })
}

export const updateCategory = (data) => {
  return request({
    url: '/api/category',
    method: 'put',
    data
  })
}

export const deleteCategory = (id) => {
  return request({
    url: `/api/category/${id}`,
    method: 'delete'
  })
}

export const getBooksByCategory = (id) => {
  return request({
    url: `/api/category/${id}/books`,
    method: 'get'
  })
}
