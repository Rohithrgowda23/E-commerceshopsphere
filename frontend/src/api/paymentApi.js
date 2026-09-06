import { axiosClient } from './axiosClient'

export const paymentApi = {
  getPaymentByOrderId: (orderId) => axiosClient.get(`/api/payments/${orderId}`).then((r) => r.data),
  refund: (orderId) => axiosClient.post(`/api/payments/${orderId}/refund`).then((r) => r.data),
}
