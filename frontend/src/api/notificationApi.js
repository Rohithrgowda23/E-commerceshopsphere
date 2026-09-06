import { axiosClient } from './axiosClient'

export const notificationApi = {
  getMyNotifications: (params) => axiosClient.get('/api/notifications', { params }).then((r) => r.data),
}
