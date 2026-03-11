import request from '@/utils/request'

export const reserveBook = (bookId) => {
  return request({
    url: '/reservation/reserve',
    method: 'post',
    data: { bookId }
  })
}

export const cancelReservation = (reservationId) => {
  return request({
    url: `/reservation/cancel/${reservationId}`,
    method: 'put'
  })
}

export const getMyReservations = () => {
  return request({
    url: '/reservation/list',
    method: 'get'
  })
}

export const getReserveCount = () => {
  return request({
    url: '/reservation/count',
    method: 'get'
  })
}

export const getAllReservations = () => {
  return request({
    url: '/reservation/admin/list',
    method: 'get'
  })
}

export const completeReservation = (reservationId) => {
  return request({
    url: `/reservation/admin/complete/${reservationId}`,
    method: 'put'
  })
}
