import { Link, useParams } from 'react-router-dom'

export default function OrderSuccess() {
  const { orderId } = useParams()

  return (
    <div className="mx-auto max-w-lg px-4 py-16 text-center">
      <p className="text-5xl">🎉</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Order placed successfully!</h1>
      <p className="mt-2 text-sm text-gray-500">
        Order <span className="font-mono">{orderId}</span> is confirmed. You&apos;ll get updates as it ships.
      </p>

      <div className="mt-8 flex justify-center gap-3">
        <Link
          to={`/orders/${orderId}`}
          className="rounded-md bg-brand-600 px-6 py-2 text-sm font-medium text-white hover:bg-brand-700"
        >
          View Order
        </Link>
        <Link
          to="/products"
          className="rounded-md border border-gray-300 px-6 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Keep Shopping
        </Link>
      </div>
    </div>
  )
}
