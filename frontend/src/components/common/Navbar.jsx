import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'

export default function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const { itemCount } = useCart()
  const navigate = useNavigate()
  const [query, setQuery] = useState('')

  const handleSearch = (e) => {
    e.preventDefault()
    if (query.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(query.trim())}`)
    }
  }

  const handleLogout = async () => {
    await logout()
    navigate('/')
  }

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-white shadow-sm">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
        <Link to="/" className="text-xl font-bold text-brand-700">
          ShopSphere
        </Link>

        <form onSubmit={handleSearch} className="flex flex-1 max-w-xl">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search products..."
            className="w-full rounded-l-md border border-gray-300 px-3 py-1.5 text-sm focus:border-brand-500 focus:outline-none"
          />
          <button
            type="submit"
            className="rounded-r-md bg-brand-600 px-4 text-sm font-medium text-white hover:bg-brand-700"
          >
            Search
          </button>
        </form>

        <nav className="flex items-center gap-4 text-sm font-medium">
          <Link to="/products" className="text-gray-700 hover:text-brand-600">
            Products
          </Link>

          {isAuthenticated && (
            <Link to="/wishlist" className="text-gray-700 hover:text-brand-600">
              Wishlist
            </Link>
          )}

          <Link to="/cart" className="relative text-gray-700 hover:text-brand-600">
            Cart
            {itemCount > 0 && (
              <span className="absolute -right-3 -top-2 flex h-5 w-5 items-center justify-center rounded-full bg-brand-600 text-xs text-white">
                {itemCount}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/orders" className="text-gray-700 hover:text-brand-600">
                Orders
              </Link>
              <Link to="/profile" className="text-gray-700 hover:text-brand-600">
                {user?.email?.split('@')[0] || 'Profile'}
              </Link>
              {isAdmin && (
                <Link to="/admin" className="text-brand-700 hover:text-brand-900">
                  Admin
                </Link>
              )}
              <button onClick={handleLogout} className="text-gray-500 hover:text-red-600">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-gray-700 hover:text-brand-600">
                Login
              </Link>
              <Link
                to="/register"
                className="rounded-md bg-brand-600 px-3 py-1.5 text-white hover:bg-brand-700"
              >
                Sign Up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
