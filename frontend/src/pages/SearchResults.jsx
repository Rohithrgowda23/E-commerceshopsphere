import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { productApi } from '../api/productApi'
import ProductCard from '../components/product/ProductCard'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function SearchResults() {
  const [searchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') || ''

  const [data, setData] = useState({ content: [], totalPages: 0 })
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await productApi.search({
        keyword,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
        page,
        size: 12,
      })
      setData(result)
    } catch {
      setError('Search failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    setPage(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword])

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, page])

  const handleFilter = (e) => {
    e.preventDefault()
    setPage(0)
    load()
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">
        Results for &ldquo;{keyword}&rdquo;
      </h1>

      <form onSubmit={handleFilter} className="mt-4 flex items-end gap-3">
        <div>
          <label className="text-xs font-medium text-gray-600">Min price</label>
          <input
            type="number"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            className="mt-1 block w-28 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
        </div>
        <div>
          <label className="text-xs font-medium text-gray-600">Max price</label>
          <input
            type="number"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            className="mt-1 block w-28 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
        </div>
        <button
          type="submit"
          className="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-brand-700"
        >
          Apply
        </button>
      </form>

      <div className="mt-6">
        {loading ? (
          <Loader />
        ) : error ? (
          <ErrorMessage message={error} onRetry={load} />
        ) : data.content.length === 0 ? (
          <p className="py-16 text-center text-gray-500">No products matched your search.</p>
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
