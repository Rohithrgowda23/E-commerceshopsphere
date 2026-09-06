import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useWishlist } from '../context/WishlistContext'
import { productApi } from '../api/productApi'
import ProductCard from '../components/product/ProductCard'
import Loader from '../components/common/Loader'

export default function Wishlist() {
  const { productIds } = useWishlist()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (productIds.length === 0) {
      setProducts([])
      setLoading(false)
      return
    }
    setLoading(true)
    Promise.all(productIds.map((id) => productApi.getById(id).catch(() => null)))
      .then((results) => setProducts(results.filter(Boolean)))
      .finally(() => setLoading(false))
  }, [productIds])

  if (loading) return <Loader />

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">My Wishlist</h1>

      {products.length === 0 ? (
        <div className="mt-8 text-center text-gray-500">
          <p>Your wishlist is empty.</p>
          <Link to="/products" className="mt-3 inline-block text-brand-600 hover:underline">
            Browse products
          </Link>
        </div>
      ) : (
        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </div>
  )
}
