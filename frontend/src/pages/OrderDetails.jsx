import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { orderApi } from '../api/orderApi'
import { formatCurrency, formatDate } from '../utils/format'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

const CANCELLABLE = ['PENDING', 'CONFIRMED', 'PROCESSING']

export default function OrderDetails() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [cancelling, setCancelling] = useState(false)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await orderApi.getOrder(id)
      setOrder(data)
    } catch {
      setError('Could not load this order.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const handleCancel = async () => {
    setCancelling(true)
    try {
      const updated = await orderApi.cancelOrder(id)
      setOrder(updated)
    } catch {
      setError('Could not cancel this order.')
    } finally {
      setCancelling(false)
    }
  }

  if (loading) return <Loader />
  if (error) return <ErrorMessage message={error} onRetry={load} />
  if (!order) return null

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Order #{order.id.slice(0, 8)}</h1>
        <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
          {order.orderStatus}
        </span>
      </div>
      <p className="mt-1 text-sm text-gray-500">Placed on {formatDate(order.createdAt)}</p>

      <div className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <h2 className="text-sm font-semibold text-gray-800">Items</h2>
        <div className="mt-3 divide-y divide-gray-100">
          {order.items.map((item) => (
            <div key={item.productId} className="flex justify-between py-2 text-sm">
              <span className="text-gray-700">
                {item.productName} × {item.quantity}
              </span>
              <span className="text-gray-900">{formatCurrency(item.subtotal)}</span>
            </div>
          ))}
        </div>
        <div className="mt-3 flex justify-between border-t border-gray-200 pt-3 text-base font-semibold">
          <span>Total</span>
          <span>{formatCurrency(order.totalAmount)}</span>
        </div>
      </div>

      <div className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <h2 className="text-sm font-semibold text-gray-800">Shipping Address</h2>
        <p className="mt-2 text-sm text-gray-600">
          {order.shippingRecipientName} — {order.shippingPhoneNumber}
          <br />
          {order.shippingAddressLine1}
          {order.shippingAddressLine2 && <>, {order.shippingAddressLine2}</>}
          <br />
          {order.shippingCity}, {order.shippingState} {order.shippingPostalCode}
          <br />
          {order.shippingCountry}
        </p>
      </div>

      <div className="mt-6 flex items-center justify-between">
        <p className="text-sm text-gray-500">
          Payment status: <span className="font-medium text-gray-800">{order.paymentStatus}</span>
        </p>
        {CANCELLABLE.includes(order.orderStatus) && (
          <button
            onClick={handleCancel}
            disabled={cancelling}
            className="rounded-md border border-red-300 px-4 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
          >
            {cancelling ? 'Cancelling...' : 'Cancel Order'}
          </button>
        )}
      </div>
    </div>
  )
}
