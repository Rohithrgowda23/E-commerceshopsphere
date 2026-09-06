import { axiosClient } from './axiosClient'

export const authApi = {
  register: (payload) => axiosClient.post('/api/auth/register', payload).then((r) => r.data),
  login: (payload) => axiosClient.post('/api/auth/login', payload).then((r) => r.data),
  refresh: (refreshToken) => axiosClient.post('/api/auth/refresh', { refreshToken }).then((r) => r.data),
  logout: (refreshToken) => axiosClient.post('/api/auth/logout', { refreshToken }).then((r) => r.data),
  loginWithGoogle: (idToken) => axiosClient.post('/api/auth/oauth/google', { idToken }).then((r) => r.data),
}
