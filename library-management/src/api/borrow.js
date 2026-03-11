import request from '@/utils/request'

export const getBorrowRecords = () => {
  return request({
    url: '/borrow/records',
    method: 'get'
  })
}

export const borrowBook = (bookId) => {
  return request({
    url: '/borrow/borrow',
    method: 'post',
    data: { bookId }
  })
}

export const returnBook = (recordId) => {
  return request({
    url: `/borrow/return/${recordId}`,
    method: 'put'
  })
}

export const renewBook = (recordId) => {
  return request({
    url: `/borrow/renew/${recordId}`,
    method: 'put'
  })
}

export const getOverdueRecords = () => {
  return request({
    url: '/borrow/overdue',
    method: 'get'
  })
}

export const getUserStats = () => {
  return request({
    url: '/borrow/stats',
    method: 'get'
  })
}

export const getAdminStats = () => {
  return request({
    url: '/borrow/admin/stats',
    method: 'get'
  })
}

export const getBorrowTrend = () => {
  return request({
    url: '/borrow/trend',
    method: 'get'
  })
}

export const getHotBooks = (limit = 10) => {
  return request({
    url: '/borrow/hot-books',
    method: 'get',
    params: { limit }
  })
}

export const deleteBorrowRecord = (recordId) => {
  return request({
    url: `/borrow/record/${recordId}`,
    method: 'delete'
  })
}