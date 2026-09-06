import { useEffect, useState } from 'react'
import { productApi } from '../../api/productApi'
import { orderApi } from '../../api/orderApi'
import { formatCurrency } from '../../utils/format'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

// There is no dedicated admin-stats endpoint in any backend service, so
// this dashboard derives its numbers from the same list endpoints the
// rest of the app uses (first page only, for a lightweight snapshot —
// a real dashboard would add a proper aggregation endpoint).
export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [productPage, orderPage] = await Promise.all([
        productApi.getAll({ page: 0, size: 1 }),
        orderApi.getMyOrders({ page: 0, size: 100 }),
      ])

      const orders = orderPage.content || []
      const revenue = orders.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
      const pending = orders.filter((o) => o.orderStatus === 'PENDING').length

      setStats({
        totalProducts: productPage.totalElements ?? 0,
        totalOrders: orderPage.totalElements ?? 0,
        revenue,
        pendingOrders: pending,
      })
    } catch {
      setError('Could not load dashboard stats.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) return <Loader />
  if (error) return <ErrorMessage message={error} onRetry={load} />

  const cards = [
    { label: 'Total Products', value: stats.totalProducts },
    { label: 'Total Orders', value: stats.totalOrders },
    { label: 'Revenue (this page)', value: formatCurrency(stats.revenue) },
    { label: 'Pending Orders', value: stats.pendingOrders },
  ]

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
      <div className="mt-6 grid grid-cols-2 gap-4 md:grid-cols-4">
        {cards.map((card) => (
          <div key={card.label} className="rounded-lg border border-gray-200 bg-white p-4">
            <p className="text-sm text-gray-500">{card.label}</p>
            <p className="mt-1 text-2xl font-bold text-gray-900">{card.value}</p>
          </div>
        ))}
      </div>
      <p className="mt-6 text-xs text-gray-400">
        Note: revenue and order counts reflect orders visible to this account's role via the
        order-service API used elsewhere in the app.
      </p>
    </div>
  )
}
