import request from '@/utils/request'

export const uploadImage = (file, type = 'book') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deleteImage = (url) => {
  return request({
    url: '/upload/image',
    method: 'delete',
    params: { url }
  })
}
