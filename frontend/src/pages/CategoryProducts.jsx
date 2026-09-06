import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { productApi } from '../api/productApi'
import ProductCard from '../components/product/ProductCard'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function CategoryProducts() {
  const { categoryId } = useParams()
  const [data, setData] = useState({ content: [], totalPages: 0 })
  const [categoryName, setCategoryName] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [result, category] = await Promise.all([
        productApi.getByCategory(categoryId, { page, size: 12 }),
        productApi.getCategories().then((cats) => cats.find((c) => c.id === categoryId)),
      ])
      setData(result)
      setCategoryName(category?.name || '')
    } catch {
      setError('Could not load this category.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categoryId, page])

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">{categoryName || 'Category'}</h1>

      <div className="mt-6">
        {loading ? (
          <Loader />
        ) : error ? (
          <ErrorMessage message={error} onRetry={load} />
        ) : data.content.length === 0 ? (
          <p className="py-16 text-center text-gray-500">No products in this category yet.</p>
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
    </div>
  )
}
