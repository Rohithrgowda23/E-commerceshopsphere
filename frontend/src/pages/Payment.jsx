import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { paymentApi } from '../api/paymentApi'
import Loader from '../components/common/Loader'

const POLL_INTERVAL_MS = 2000
const MAX_POLLS = 30 // ~1 minute

export default function Payment() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [payment, setPayment] = useState(null)
  const [status, setStatus] = useState('waiting') // waiting | success | failed | timeout
  const pollCount = useRef(0)

  useEffect(() => {
    let timer

    const poll = async () => {
      try {
        const data = await paymentApi.getPaymentByOrderId(orderId)
        setPayment(data)

        if (data.status === 'SUCCESS') {
          setStatus('success')
          return
        }
        if (data.status === 'FAILED') {
          setStatus('failed')
          return
        }
      } catch {
        // A 404 here just means payment-service hasn't processed the
        // ORDER_CREATED event yet — keep polling.
      }

      pollCount.current += 1
      if (pollCount.current >= MAX_POLLS) {
        setStatus('timeout')
        return
      }
      timer = setTimeout(poll, POLL_INTERVAL_MS)
    }

    poll()
    return () => clearTimeout(timer)
  }, [orderId])

  useEffect(() => {
    if (status === 'success') {
      const t = setTimeout(() => navigate(`/order-success/${orderId}`), 800)
      return () => clearTimeout(t)
    }
  }, [status, orderId, navigate])

  return (
    <div className="mx-auto max-w-lg px-4 py-16 text-center">
      {status === 'waiting' && (
        <>
          <Loader label="Processing your payment..." />
          <p className="text-sm text-gray-500">This usually takes a few seconds.</p>
        </>
      )}

      {status === 'success' && (
        <div>
          <p className="text-4xl">✅</p>
          <h1 className="mt-3 text-xl font-bold text-gray-900">Payment successful!</h1>
          <p className="mt-1 text-sm text-gray-500">Redirecting to your order...</p>
        </div>
      )}

      {status === 'failed' && (
        <div>
          <p className="text-4xl">❌</p>
          <h1 className="mt-3 text-xl font-bold text-gray-900">Payment failed</h1>
          <p className="mt-1 text-sm text-gray-500">{payment?.failureReason || 'The charge could not be completed.'}</p>
          <Link
            to="/orders"
            className="mt-6 inline-block rounded-md bg-brand-600 px-6 py-2 text-sm font-medium text-white hover:bg-brand-700"
          >
            View my orders
          </Link>
        </div>
      )}

      {status === 'timeout' && (
        <div>
          <p className="text-4xl">⏳</p>
          <h1 className="mt-3 text-xl font-bold text-gray-900">Still processing</h1>
          <p className="mt-1 text-sm text-gray-500">
            This is taking longer than expected. Check your order status shortly.
          </p>
          <Link
            to="/orders"
            className="mt-6 inline-block rounded-md bg-brand-600 px-6 py-2 text-sm font-medium text-white hover:bg-brand-700"
          >
            View my orders
          </Link>
        </div>
      )}
    </div>
  )
}
