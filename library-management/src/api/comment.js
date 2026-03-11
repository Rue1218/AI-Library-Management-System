import request from '@/utils/request'

export const getComments = (bookId) => {
  return request({
    url: `/comments/book/${bookId}`,
    method: 'get'
  })
}

export const getBookRating = (bookId) => {
  return request({
    url: `/comments/book/${bookId}/rating`,
    method: 'get'
  })
}

export const addComment = (data) => {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

export const likeComment = (commentId) => {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'post'
  })
}

export const unlikeComment = (commentId) => {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'delete'
  })
}
