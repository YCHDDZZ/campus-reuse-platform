import http from './http'

export const uploadFileToMinio = async (file, folder = 'common') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  return await http.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const deleteMinioFile = async (objectName) => {
  return await http.delete('/files', {
    params: { objectName },
  })
}
