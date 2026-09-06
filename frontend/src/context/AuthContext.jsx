import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { authApi } from '../api/authApi'
import { setTokens, clearTokens, getAccessToken } from '../api/axiosClient'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    // If a token exists but we somehow lost the user object (e.g. cleared
    // manually), keep them logged out rather than guessing at identity.
    if (!getAccessToken() && user) {
      setUser(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const persistSession = (authResponse) => {
    setTokens({ accessToken: authResponse.accessToken, refreshToken: authResponse.refreshToken })
    const nextUser = {
      userId: authResponse.userId,
      email: authResponse.email,
      roles: authResponse.roles || [],
    }
    localStorage.setItem('user', JSON.stringify(nextUser))
    setUser(nextUser)
  }

  const login = useCallback(async (credentials) => {
    setLoading(true)
    setError(null)
    try {
      const data = await authApi.login(credentials)
      persistSession(data)
      return data
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please check your credentials.'
      setError(message)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const loginWithGoogle = useCallback(async (idToken) => {
    setLoading(true)
    setError(null)
    try {
      const data = await authApi.loginWithGoogle(idToken)
      persistSession(data)
      return data
    } catch (err) {
      const message = err.response?.data?.message || 'Google sign-in failed.'
      setError(message)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const register = useCallback(async (payload) => {
    setLoading(true)
    setError(null)
    try {
      const data = await authApi.register(payload)
      persistSession(data)
      return data
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed.'
      setError(message)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    try {
      if (refreshToken) await authApi.logout(refreshToken)
    } catch {
      // Even if the server call fails, still clear the local session.
    }
    clearTokens()
    setUser(null)
  }, [])

  const isAdmin = user?.roles?.includes('ADMIN') ?? false
  const isAuthenticated = !!user

  return (
      <AuthContext.Provider
          value={{ user, loading, error, login, register, loginWithGoogle, logout, isAdmin, isAuthenticated }}
      >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
