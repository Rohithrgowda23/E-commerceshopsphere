import { axiosClient } from './axiosClient'

export const inventoryApi = {
  getInventory: (productId) => axiosClient.get(`/api/inventory/${productId}`).then((r) => r.data),
  addStock: (payload) => axiosClient.post('/api/inventory/add', payload).then((r) => r.data),
}
