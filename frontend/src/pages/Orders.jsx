import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../api/orderApi'
import { formatCurrency, formatDate } from '../utils/format'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

const statusColors = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PROCESSING: 'bg-blue-100 text-blue-700',
  SHIPPED: 'bg-indigo-100 text-indigo-700',
  OUT_FOR_DELIVERY: 'bg-indigo-100 text-indigo-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
}

export default function Orders() {
  const [data, setData] = useState({ content: [], totalPages: 0 })
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await orderApi.getMyOrders({ page, size: 10 })
      setData(result)
    } catch {
      setError('Could not load your orders.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page])

  if (loading) return <Loader />
  if (error) return <ErrorMessage message={error} onRetry={load} />

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">My Orders</h1>

      {data.content.length === 0 ? (
        <p className="mt-8 text-center text-gray-500">You haven&apos;t placed any orders yet.</p>
      ) : (
        <div className="mt-6 space-y-3">
          {data.content.map((order) => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4 hover:shadow-sm"
            >
              <div>
                <p className="text-sm font-medium text-gray-800">Order #{order.id.slice(0, 8)}</p>
                <p className="text-xs text-gray-500">{formatDate(order.createdAt)}</p>
              </div>
              <div className="flex items-center gap-4">
                <span className="text-sm font-semibold text-gray-900">
                  {formatCurrency(order.totalAmount)}
                </span>
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${
                    statusColors[order.orderStatus] || 'bg-gray-100 text-gray-700'
                  }`}
                >
                  {order.orderStatus}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}

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
    </div>
  )
}
