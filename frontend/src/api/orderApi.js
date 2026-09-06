import { axiosClient } from './axiosClient'

export const orderApi = {
  createOrder: (payload) => axiosClient.post('/api/orders', payload).then((r) => r.data),
  getMyOrders: (params) => axiosClient.get('/api/orders', { params }).then((r) => r.data),
  getOrder: (id) => axiosClient.get(`/api/orders/${id}`).then((r) => r.data),
  cancelOrder: (id) => axiosClient.put(`/api/orders/${id}/cancel`).then((r) => r.data),
}
