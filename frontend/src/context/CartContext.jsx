import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { cartApi } from '../api/cartApi'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [cart, setCart] = useState({ items: [], total: 0 })
  const [loading, setLoading] = useState(false)

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart({ items: [], total: 0 })
      return
    }
    setLoading(true)
    try {
      const data = await cartApi.getCart()
      setCart(data)
    } catch {
      // A logged-in-but-cart-unreachable state shouldn't crash the app —
      // leave the last known cart in place.
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    refreshCart()
  }, [refreshCart])

  const addItem = useCallback(async (productId, quantity = 1) => {
    const data = await cartApi.addItem({ productId, quantity })
    setCart(data)
    return data
  }, [])

  const updateItem = useCallback(async (productId, quantity) => {
    const data = await cartApi.updateItem(productId, quantity)
    setCart(data)
    return data
  }, [])

  const removeItem = useCallback(async (productId) => {
    const data = await cartApi.removeItem(productId)
    setCart(data)
    return data
  }, [])

  const clearCart = useCallback(async () => {
    await cartApi.clearCart()
    setCart({ items: [], total: 0 })
  }, [])

  const itemCount = cart.items?.reduce((sum, item) => sum + item.quantity, 0) ?? 0

  return (
    <CartContext.Provider
      value={{ cart, loading, itemCount, refreshCart, addItem, updateItem, removeItem, clearCart }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within a CartProvider')
  return ctx
}
