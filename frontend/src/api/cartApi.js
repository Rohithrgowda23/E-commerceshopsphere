import { axiosClient } from './axiosClient'

export const cartApi = {
  getCart: () => axiosClient.get('/api/cart').then((r) => r.data),
  addItem: (payload) => axiosClient.post('/api/cart/items', payload).then((r) => r.data),
  updateItem: (productId, quantity) =>
    axiosClient.put(`/api/cart/items/${productId}`, { quantity }).then((r) => r.data),
  removeItem: (productId) => axiosClient.delete(`/api/cart/items/${productId}`).then((r) => r.data),
  clearCart: () => axiosClient.delete('/api/cart').then((r) => r.data),
}
