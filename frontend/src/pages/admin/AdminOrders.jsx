import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../../api/orderApi'
import { formatCurrency, formatDate } from '../../utils/format'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

export default function AdminOrders() {
  const [data, setData] = useState({ content: [], totalPages: 0 })
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      // NOTE: order-service's GET /api/orders is scoped to "the caller's
      // own orders" regardless of role — there is no platform-wide
      // "list all orders" endpoint in the current backend. This page
      // therefore shows the logged-in admin's own order history, not
      // every customer's orders. Adding a true admin listing would mean
      // adding an admin-only endpoint/repository method on order-service.
      const result = await orderApi.getMyOrders({ page, size: 15 })
      setData(result)
    } catch {
      setError('Could not load orders.')
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
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Orders</h1>
      <p className="mt-1 text-xs text-amber-600">
        Showing orders visible to this account. The backend does not yet expose a platform-wide
        order listing endpoint — see the frontend README for details.
      </p>

      <div className="mt-6 overflow-hidden rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
            <tr>
              <th className="px-4 py-2">Order</th>
              <th className="px-4 py-2">Date</th>
              <th className="px-4 py-2">Total</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data.content.map((order) => (
              <tr key={order.id}>
                <td className="px-4 py-2">
                  <Link to={`/orders/${order.id}`} className="text-brand-600 hover:underline">
                    #{order.id.slice(0, 8)}
                  </Link>
                </td>
                <td className="px-4 py-2">{formatDate(order.createdAt)}</td>
                <td className="px-4 py-2">{formatCurrency(order.totalAmount)}</td>
                <td className="px-4 py-2">{order.orderStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {data.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-2">
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
