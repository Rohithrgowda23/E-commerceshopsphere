import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { productApi } from '../api/productApi'
import ProductCard from '../components/product/ProductCard'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function Home() {
  const [featured, setFeatured] = useState([])
  const [latest, setLatest] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [latestPage, discountedPage, categoryList] = await Promise.all([
        productApi.getAll({ page: 0, size: 8, sortBy: 'createdAt', direction: 'desc' }),
        productApi.search({ page: 0, size: 8, sortBy: 'rating', direction: 'desc' }),
        productApi.getCategories(),
      ])
      setLatest(latestPage.content || [])
      setFeatured(discountedPage.content || [])
      setCategories(categoryList || [])
    } catch {
      setError('Could not load the storefront right now.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) return <Loader label="Loading storefront..." />
  if (error) return <ErrorMessage message={error} onRetry={load} />

  return (
    <div>
      <section className="bg-gradient-to-r from-brand-700 to-brand-900 py-16 text-white">
        <div className="mx-auto max-w-7xl px-4 text-center">
          <h1 className="text-4xl font-bold">Everything you need, delivered fast.</h1>
          <p className="mt-3 text-brand-100">
            Browse thousands of products across every category.
          </p>
          <Link
            to="/products"
            className="mt-6 inline-block rounded-md bg-white px-6 py-2 font-medium text-brand-700 hover:bg-brand-50"
          >
            Shop Now
          </Link>
        </div>
      </section>

      {categories.length > 0 && (
        <section className="mx-auto max-w-7xl px-4 py-10">
          <h2 className="mb-4 text-xl font-semibold text-gray-900">Shop by Category</h2>
          <div className="flex flex-wrap gap-3">
            {categories.map((cat) => (
              <Link
                key={cat.id}
                to={`/category/${cat.id}`}
                className="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:border-brand-500 hover:text-brand-600"
              >
                {cat.name}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="mx-auto max-w-7xl px-4 py-6">
        <h2 className="mb-4 text-xl font-semibold text-gray-900">Featured Products</h2>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
          {featured.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-6">
        <h2 className="mb-4 text-xl font-semibold text-gray-900">Latest Arrivals</h2>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
          {latest.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>
    </div>
  )
}
