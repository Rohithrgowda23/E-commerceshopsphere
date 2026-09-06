import { useEffect, useState } from 'react'
import { productApi } from '../../api/productApi'
import { inventoryApi } from '../../api/inventoryApi'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

export default function AdminInventory() {
  const [products, setProducts] = useState([])
  const [stockByProduct, setStockByProduct] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [addForm, setAddForm] = useState({}) // productId -> quantity string
  const [savingId, setSavingId] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const productPage = await productApi.getAll({ page: 0, size: 50 })
      const list = productPage.content || []
      setProducts(list)

      const stockEntries = await Promise.all(
        list.map((p) =>
          inventoryApi
            .getInventory(p.id)
            .then((inv) => [p.id, inv])
            .catch(() => [p.id, null])
        )
      )
      setStockByProduct(Object.fromEntries(stockEntries))
    } catch {
      setError('Could not load inventory.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const handleAddStock = async (productId) => {
    const quantity = Number(addForm[productId])
    if (!quantity || quantity <= 0) return

    setSavingId(productId)
    try {
      const updated = await inventoryApi.addStock({ productId, quantity })
      setStockByProduct((prev) => ({ ...prev, [productId]: updated }))
      setAddForm((prev) => ({ ...prev, [productId]: '' }))
    } catch {
      setError('Could not add stock for this product.')
    } finally {
      setSavingId(null)
    }
  }

  if (loading) return <Loader />
  if (error) return <ErrorMessage message={error} onRetry={load} />

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Inventory</h1>

      <div className="mt-6 overflow-hidden rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
            <tr>
              <th className="px-4 py-2">Product</th>
              <th className="px-4 py-2">Available</th>
              <th className="px-4 py-2">Reserved</th>
              <th className="px-4 py-2">Add Stock</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {products.map((p) => {
              const stock = stockByProduct[p.id]
              const isLow = stock != null && stock.availableQuantity < 10
              return (
                <tr key={p.id}>
                  <td className="px-4 py-2">{p.name}</td>
                  <td className={`px-4 py-2 ${isLow ? 'font-semibold text-red-600' : ''}`}>
                    {stock ? stock.availableQuantity : '—'}
                    {isLow && ' (low)'}
                  </td>
                  <td className="px-4 py-2">{stock ? stock.reservedQuantity : '—'}</td>
                  <td className="px-4 py-2">
                    <div className="flex gap-2">
                      <input
                        type="number"
                        min={1}
                        placeholder="Qty"
                        value={addForm[p.id] || ''}
                        onChange={(e) => setAddForm({ ...addForm, [p.id]: e.target.value })}
                        className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm"
                      />
                      <button
                        onClick={() => handleAddStock(p.id)}
                        disabled={savingId === p.id}
                        className="rounded-md bg-brand-600 px-3 py-1 text-xs font-medium text-white hover:bg-brand-700 disabled:bg-gray-300"
                      >
                        Add
                      </button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
