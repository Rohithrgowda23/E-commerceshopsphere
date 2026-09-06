import { useEffect, useState } from 'react'
import { productApi } from '../../api/productApi'
import { formatCurrency } from '../../utils/format'
import Loader from '../../components/common/Loader'
import ErrorMessage from '../../components/common/ErrorMessage'

const emptyForm = {
  name: '',
  description: '',
  price: '',
  discountPrice: '',
  brand: '',
  categoryId: '',
  imageUrl: '',
  available: true,
}

export default function AdminProducts() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [productPage, cats] = await Promise.all([
        productApi.getAll({ page: 0, size: 50, sortBy: 'createdAt', direction: 'desc' }),
        productApi.getCategories(),
      ])
      setProducts(productPage.content || [])
      setCategories(cats || [])
    } catch {
      setError('Could not load products.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const openNewForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setShowForm(true)
  }

  const openEditForm = (product) => {
    setForm({
      name: product.name,
      description: product.description || '',
      price: product.price,
      discountPrice: product.discountPrice || '',
      brand: product.brand || '',
      categoryId: product.categoryId,
      imageUrl: product.imageUrl || '',
      available: product.available,
    })
    setEditingId(product.id)
    setShowForm(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        discountPrice: form.discountPrice ? Number(form.discountPrice) : null,
      }
      if (editingId) {
        await productApi.update(editingId, payload)
      } else {
        await productApi.create(payload)
      }
      setShowForm(false)
      await load()
    } catch {
      setError('Could not save this product.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    try {
      await productApi.remove(id)
      await load()
    } catch {
      setError('Could not delete this product.')
    }
  }

  if (loading) return <Loader />

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Products</h1>
        <button
          onClick={openNewForm}
          className="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-brand-700"
        >
          New Product
        </button>
      </div>

      {error && <ErrorMessage message={error} onRetry={load} />}

      <div className="mt-6 overflow-hidden rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
            <tr>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Price</th>
              <th className="px-4 py-2">Available</th>
              <th className="px-4 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {products.map((p) => (
              <tr key={p.id}>
                <td className="px-4 py-2">{p.name}</td>
                <td className="px-4 py-2">{formatCurrency(p.discountPrice ?? p.price)}</td>
                <td className="px-4 py-2">{p.available ? 'Yes' : 'No'}</td>
                <td className="px-4 py-2 text-right">
                  <button onClick={() => openEditForm(p)} className="mr-3 text-brand-600 hover:underline">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(p.id)} className="text-red-500 hover:underline">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="mt-6 grid grid-cols-2 gap-3 rounded-lg border border-gray-200 bg-white p-4"
        >
          <h2 className="col-span-2 text-sm font-semibold text-gray-800">
            {editingId ? 'Edit Product' : 'New Product'}
          </h2>
          <input
            required
            placeholder="Name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <textarea
            placeholder="Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
            rows={3}
          />
          <input
            required
            type="number"
            step="0.01"
            placeholder="Price"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            type="number"
            step="0.01"
            placeholder="Discount price (optional)"
            value={form.discountPrice}
            onChange={(e) => setForm({ ...form, discountPrice: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            placeholder="Brand"
            value={form.brand}
            onChange={(e) => setForm({ ...form, brand: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <select
            required
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">Select category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <input
            placeholder="Image URL"
            value={form.imageUrl}
            onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <label className="col-span-2 flex items-center gap-2 text-sm text-gray-700">
            <input
              type="checkbox"
              checked={form.available}
              onChange={(e) => setForm({ ...form, available: e.target.checked })}
            />
            Available for purchase
          </label>

          <div className="col-span-2 flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-brand-700 disabled:bg-gray-300"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="rounded-md border border-gray-300 px-4 py-1.5 text-sm text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  )
}
