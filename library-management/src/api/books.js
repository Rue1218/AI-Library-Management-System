import request from '@/utils/request'

export const getBooks = (params) => {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

export const getBookDetail = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

export const createBook = (data) => {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

export const updateBook = (id, data) => {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data
  })
}

export const deleteBook = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}

export const getBookCategories = () => {
  return request({
    url: '/books/categories',
    method: 'get'
  })
}

export const getRecommendedBooks = (limit = 8) => {
  return request({
    url: '/books/recommended',
    method: 'get',
    params: { limit }
  })
}