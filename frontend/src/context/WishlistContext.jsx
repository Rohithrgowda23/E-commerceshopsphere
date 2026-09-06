import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { useAuth } from './AuthContext'

const WishlistContext = createContext(null)

// There is no wishlist-service in the backend architecture, so this is
// intentionally client-side only, keyed per user id in localStorage.
// It mirrors the shape a real backend-backed wishlist would have
// (productId list) so swapping in a real API later is a small change
// confined to this file.
function storageKey(userId) {
  return `wishlist:${userId || 'guest'}`
}

export function WishlistProvider({ children }) {
  const { user } = useAuth()
  const [productIds, setProductIds] = useState([])

  useEffect(() => {
    const stored = localStorage.getItem(storageKey(user?.userId))
    setProductIds(stored ? JSON.parse(stored) : [])
  }, [user])

  const persist = (next) => {
    setProductIds(next)
    localStorage.setItem(storageKey(user?.userId), JSON.stringify(next))
  }

  const toggle = useCallback(
    (productId) => {
      const exists = productIds.includes(productId)
      const next = exists ? productIds.filter((id) => id !== productId) : [...productIds, productId]
      persist(next)
    },
    [productIds] // eslint-disable-line react-hooks/exhaustive-deps
  )

  const isWishlisted = useCallback((productId) => productIds.includes(productId), [productIds])

  return (
    <WishlistContext.Provider value={{ productIds, toggle, isWishlisted }}>
      {children}
    </WishlistContext.Provider>
  )
}

export function useWishlist() {
  const ctx = useContext(WishlistContext)
  if (!ctx) throw new Error('useWishlist must be used within a WishlistProvider')
  return ctx
}
