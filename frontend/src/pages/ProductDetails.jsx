import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { productApi } from '../api/productApi'
import { inventoryApi } from '../api/inventoryApi'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { useAuth } from '../context/AuthContext'
import { formatCurrency } from '../utils/format'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function ProductDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()
  const { toggle, isWishlisted } = useWishlist()

  const [product, setProduct] = useState(null)
  const [stock, setStock] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actionMessage, setActionMessage] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await productApi.getById(id)
      setProduct(data)
      // Stock lookup is best-effort — the product page still works even
      // if inventory-service is briefly unreachable, just without a
      // precise "X left" indicator.
      inventoryApi
        .getInventory(id)
        .then((inv) => setStock(inv.availableQuantity))
        .catch(() => setStock(null))
    } catch {
      setError('Product not found.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    await addItem(product.id, quantity)
    setActionMessage('Added to cart.')
  }

  const handleBuyNow = async () => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    await addItem(product.id, quantity)
    navigate('/checkout')
  }

  if (loading) return <Loader />
  if (error) return <ErrorMessage message={error} onRetry={load} />
  if (!product) return null

  const price = product.discountPrice ?? product.price
  const hasDiscount = product.discountPrice != null && product.discountPrice < product.price

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
        <div className="aspect-square overflow-hidden rounded-lg bg-gray-100">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center text-gray-400">No image</div>
          )}
        </div>

        <div>
          <h1 className="text-2xl font-bold text-gray-900">{product.name}</h1>
          {product.brand && <p className="mt-1 text-sm text-gray-500">{product.brand}</p>}

          <div className="mt-3 flex items-center gap-2">
            <span className="text-2xl font-bold text-gray-900">{formatCurrency(price)}</span>
            {hasDiscount && (
              <span className="text-base text-gray-400 line-through">
                {formatCurrency(product.price)}
              </span>
            )}
          </div>

          {product.rating != null && (
            <p className="mt-1 text-sm text-yellow-600">★ {Number(product.rating).toFixed(1)}</p>
          )}

          <p className="mt-4 text-sm leading-relaxed text-gray-600">{product.description}</p>

          <p className="mt-4 text-sm">
            {product.available ? (
              <span className="text-green-600">
                In stock{stock != null ? ` (${stock} available)` : ''}
              </span>
            ) : (
              <span className="text-red-500">Out of stock</span>
            )}
          </p>

          <div className="mt-4 flex items-center gap-3">
            <label className="text-sm font-medium text-gray-700">Qty</label>
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              className="w-20 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
            />
            <button
              onClick={() => toggle(product.id)}
              className="text-sm text-gray-500 hover:text-red-500"
            >
              {isWishlisted(product.id) ? '❤️ Wishlisted' : '🤍 Add to wishlist'}
            </button>
          </div>

          {actionMessage && <p className="mt-2 text-sm text-green-600">{actionMessage}</p>}

          <div className="mt-6 flex gap-3">
            <button
              onClick={handleAddToCart}
              disabled={!product.available}
              className="flex-1 rounded-md border border-brand-600 py-2 text-sm font-medium text-brand-700 hover:bg-brand-50 disabled:cursor-not-allowed disabled:border-gray-300 disabled:text-gray-400"
            >
              Add to Cart
            </button>
            <button
              onClick={handleBuyNow}
              disabled={!product.available}
              className="flex-1 rounded-md bg-brand-600 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-gray-300"
            >
              Buy Now
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
