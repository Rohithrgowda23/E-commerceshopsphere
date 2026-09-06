import { Link } from 'react-router-dom'
import { formatCurrency } from '../../utils/format'
import { useCart } from '../../context/CartContext'
import { useWishlist } from '../../context/WishlistContext'
import { useAuth } from '../../context/AuthContext'

export default function ProductCard({ product }) {
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()
  const { toggle, isWishlisted } = useWishlist()

  const price = product.discountPrice ?? product.price
  const hasDiscount = product.discountPrice != null && product.discountPrice < product.price

  const handleAddToCart = async (e) => {
    e.preventDefault()
    if (!isAuthenticated) return
    await addItem(product.id, 1)
  }

  const handleWishlist = (e) => {
    e.preventDefault()
    toggle(product.id)
  }

  return (
    <Link
      to={`/products/${product.id}`}
      className="group relative flex flex-col overflow-hidden rounded-lg border border-gray-200 bg-white transition hover:shadow-md"
    >
      <div className="aspect-square w-full overflow-hidden bg-gray-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="h-full w-full object-cover transition group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">No image</div>
        )}
      </div>

      <button
        onClick={handleWishlist}
        className="absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 shadow"
        aria-label="Toggle wishlist"
      >
        {isWishlisted(product.id) ? '❤️' : '🤍'}
      </button>

      <div className="flex flex-1 flex-col p-3">
        <h3 className="line-clamp-2 text-sm font-medium text-gray-800">{product.name}</h3>
        {product.brand && <p className="mt-0.5 text-xs text-gray-400">{product.brand}</p>}

        <div className="mt-2 flex items-center gap-2">
          <span className="text-base font-semibold text-gray-900">{formatCurrency(price)}</span>
          {hasDiscount && (
            <span className="text-xs text-gray-400 line-through">{formatCurrency(product.price)}</span>
          )}
        </div>

        {!product.available && (
          <span className="mt-1 text-xs font-medium text-red-500">Out of stock</span>
        )}

        <button
          onClick={handleAddToCart}
          disabled={!product.available}
          className="mt-3 rounded-md bg-brand-600 py-1.5 text-sm font-medium text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-gray-300"
        >
          Add to Cart
        </button>
      </div>
    </Link>
  )
}
