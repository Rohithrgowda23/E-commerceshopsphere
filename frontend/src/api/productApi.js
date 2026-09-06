import { axiosClient } from './axiosClient'

export const productApi = {
  getAll: (params) => axiosClient.get('/api/products', { params }).then((r) => r.data),
  getById: (id) => axiosClient.get(`/api/products/${id}`).then((r) => r.data),
  search: (params) => axiosClient.get('/api/products/search', { params }).then((r) => r.data),
  getByCategory: (categoryId, params) =>
    axiosClient.get(`/api/products/category/${categoryId}`, { params }).then((r) => r.data),
  create: (payload) => axiosClient.post('/api/products', payload).then((r) => r.data),
  update: (id, payload) => axiosClient.put(`/api/products/${id}`, payload).then((r) => r.data),
  remove: (id) => axiosClient.delete(`/api/products/${id}`).then((r) => r.data),

  getCategories: () => axiosClient.get('/api/categories').then((r) => r.data),
  createCategory: (payload) => axiosClient.post('/api/categories', payload).then((r) => r.data),
}
