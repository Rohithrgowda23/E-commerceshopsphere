import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../api/userApi'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

const emptyForm = {
  label: '',
  recipientName: '',
  phoneNumber: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
  isDefault: false,
}

export default function Addresses() {
  const { user } = useAuth()
  const [addresses, setAddresses] = useState([])
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
      const data = await userApi.getAddresses(user.userId)
      setAddresses(data)
    } catch {
      setError('Could not load your addresses.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const openNewForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setShowForm(true)
  }

  const openEditForm = (address) => {
    setForm(address)
    setEditingId(address.id)
    setShowForm(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editingId) {
        await userApi.updateAddress(user.userId, editingId, form)
      } else {
        await userApi.addAddress(user.userId, form)
      }
      setShowForm(false)
      await load()
    } catch {
      setError('Could not save this address.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (addressId) => {
    try {
      await userApi.deleteAddress(user.userId, addressId)
      await load()
    } catch {
      setError('Could not delete this address.')
    }
  }

  if (loading) return <Loader />

  return (
    <div className="mx-auto max-w-2xl px-4 py-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">My Addresses</h1>
        <button
          onClick={openNewForm}
          className="rounded-md bg-brand-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-brand-700"
        >
          Add Address
        </button>
      </div>

      {error && <ErrorMessage message={error} onRetry={load} />}

      <div className="mt-6 space-y-3">
        {addresses.map((address) => (
          <div key={address.id} className="rounded-lg border border-gray-200 bg-white p-4">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-sm font-semibold text-gray-800">
                  {address.label} {address.isDefault && <span className="text-xs text-brand-600">(Default)</span>}
                </p>
                <p className="mt-1 text-sm text-gray-600">
                  {address.recipientName} — {address.phoneNumber}
                  <br />
                  {address.addressLine1}
                  {address.addressLine2 && <>, {address.addressLine2}</>}
                  <br />
                  {address.city}, {address.state} {address.postalCode}, {address.country}
                </p>
              </div>
              <div className="flex gap-2 text-sm">
                <button onClick={() => openEditForm(address)} className="text-brand-600 hover:underline">
                  Edit
                </button>
                <button onClick={() => handleDelete(address.id)} className="text-red-500 hover:underline">
                  Delete
                </button>
              </div>
            </div>
          </div>
        ))}
        {addresses.length === 0 && <p className="text-center text-gray-500">No saved addresses yet.</p>}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="mt-6 grid grid-cols-2 gap-3 rounded-lg border border-gray-200 bg-white p-4"
        >
          <h2 className="col-span-2 text-sm font-semibold text-gray-800">
            {editingId ? 'Edit Address' : 'New Address'}
          </h2>
          <input
            required
            placeholder="Label (e.g. Home)"
            value={form.label}
            onChange={(e) => setForm({ ...form, label: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm sm:col-span-1"
          />
          <input
            required
            placeholder="Recipient name"
            value={form.recipientName}
            onChange={(e) => setForm({ ...form, recipientName: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm sm:col-span-1"
          />
          <input
            required
            placeholder="Phone number"
            value={form.phoneNumber}
            onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            required
            placeholder="Address line 1"
            value={form.addressLine1}
            onChange={(e) => setForm({ ...form, addressLine1: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            placeholder="Address line 2"
            value={form.addressLine2}
            onChange={(e) => setForm({ ...form, addressLine2: e.target.value })}
            className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            required
            placeholder="City"
            value={form.city}
            onChange={(e) => setForm({ ...form, city: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            required
            placeholder="State"
            value={form.state}
            onChange={(e) => setForm({ ...form, state: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            required
            placeholder="Postal code"
            value={form.postalCode}
            onChange={(e) => setForm({ ...form, postalCode: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <input
            required
            placeholder="Country"
            value={form.country}
            onChange={(e) => setForm({ ...form, country: e.target.value })}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          <label className="col-span-2 flex items-center gap-2 text-sm text-gray-700">
            <input
              type="checkbox"
              checked={form.isDefault}
              onChange={(e) => setForm({ ...form, isDefault: e.target.checked })}
            />
            Set as default address
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
