import { useEffect, useState } from 'react'
import { productApi } from '../api/productApi'
import ProductCard from '../components/product/ProductCard'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function Products() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [sortBy, setSortBy] = useState('createdAt')
  const [direction, setDirection] = useState('desc')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await productApi.getAll({ page, size: 12, sortBy, direction })
      setData(result)
    } catch {
      setError('Could not load products.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, sortBy, direction])

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">All Products</h1>
        <select
          value={`${sortBy}:${direction}`}
          onChange={(e) => {
            const [s, d] = e.target.value.split(':')
            setSortBy(s)
            setDirection(d)
            setPage(0)
          }}
          className="rounded-md border border-gray-300 px-3 py-1.5 text-sm"
        >
          <option value="createdAt:desc">Newest</option>
          <option value="price:asc">Price: Low to High</option>
          <option value="price:desc">Price: High to Low</option>
          <option value="rating:desc">Top Rated</option>
        </select>
      </div>

      {loading ? (
        <Loader />
      ) : error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : data.content.length === 0 ? (
        <p className="py-16 text-center text-gray-500">No products found.</p>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
            {data.content.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>

          {data.totalPages > 1 && (
            <div className="mt-8 flex items-center justify-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {data.totalPages}
              </span>
              <button
                disabled={data.last}
                onClick={() => setPage((p) => p + 1)}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
