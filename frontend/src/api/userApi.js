import { axiosClient } from './axiosClient'

export const userApi = {
  getProfile: (userId) => axiosClient.get(`/api/users/${userId}`).then((r) => r.data),
  updateProfile: (userId, payload) => axiosClient.put(`/api/users/${userId}`, payload).then((r) => r.data),

  getAddresses: (userId) => axiosClient.get(`/api/users/${userId}/addresses`).then((r) => r.data),
  addAddress: (userId, payload) => axiosClient.post(`/api/users/${userId}/addresses`, payload).then((r) => r.data),
  updateAddress: (userId, addressId, payload) =>
    axiosClient.put(`/api/users/${userId}/addresses/${addressId}`, payload).then((r) => r.data),
  deleteAddress: (userId, addressId) =>
    axiosClient.delete(`/api/users/${userId}/addresses/${addressId}`).then((r) => r.data),
}
