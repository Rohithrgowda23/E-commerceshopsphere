import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { userApi } from '../api/userApi'
import { orderApi } from '../api/orderApi'
import { formatCurrency } from '../utils/format'
import Loader from '../components/common/Loader'

const emptyAddress = {
  shippingRecipientName: '',
  shippingPhoneNumber: '',
  shippingAddressLine1: '',
  shippingAddressLine2: '',
  shippingCity: '',
  shippingState: '',
  shippingPostalCode: '',
  shippingCountry: '',
}

export default function Checkout() {
  const { user } = useAuth()
  const { cart, clearCart: clearCartState } = useCart()
  const navigate = useNavigate()

  const [savedAddresses, setSavedAddresses] = useState([])
  const [selectedAddressId, setSelectedAddressId] = useState('new')
  const [form, setForm] = useState(emptyAddress)
  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    userApi
      .getAddresses(user.userId)
      .then((addresses) => setSavedAddresses(addresses))
      .catch(() => setSavedAddresses([]))
  }, [user.userId])

  const applySavedAddress = (addressId) => {
    setSelectedAddressId(addressId)
    if (addressId === 'new') {
      setForm(emptyAddress)
      return
    }
    const address = savedAddresses.find((a) => a.id === addressId)
    if (address) {
      setForm({
        shippingRecipientName: address.recipientName,
        shippingPhoneNumber: address.phoneNumber,
        shippingAddressLine1: address.addressLine1,
        shippingAddressLine2: address.addressLine2 || '',
        shippingCity: address.city,
        shippingState: address.state,
        shippingPostalCode: address.postalCode,
        shippingCountry: address.country,
      })
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setPlacing(true)
    setError(null)
    try {
      const order = await orderApi.createOrder(form)
      // The order was created successfully server-side, which already
      // clears the cart there — sync local state so the navbar badge
      // updates immediately without waiting on a refetch.
      clearCartState().catch(() => {})
      navigate(`/payment/${order.id}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not place your order. Please try again.')
    } finally {
      setPlacing(false)
    }
  }

  if (!cart.items || cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-center text-gray-500">
        Your cart is empty — nothing to check out.
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">Checkout</h1>

      <div className="mt-6 grid grid-cols-1 gap-8 md:grid-cols-3">
        <form onSubmit={handleSubmit} className="md:col-span-2">
          <h2 className="text-lg font-semibold text-gray-800">Shipping Address</h2>

          {savedAddresses.length > 0 && (
            <select
              value={selectedAddressId}
              onChange={(e) => applySavedAddress(e.target.value)}
              className="mt-3 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="new">Enter a new address</option>
              {savedAddresses.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.label} — {a.addressLine1}, {a.city}
                </option>
              ))}
            </select>
          )}

          <div className="mt-4 grid grid-cols-2 gap-3">
            <input
              required
              placeholder="Recipient name"
              value={form.shippingRecipientName}
              onChange={(e) => setForm({ ...form, shippingRecipientName: e.target.value })}
              className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm sm:col-span-1"
            />
            <input
              required
              placeholder="Phone number"
              value={form.shippingPhoneNumber}
              onChange={(e) => setForm({ ...form, shippingPhoneNumber: e.target.value })}
              className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm sm:col-span-1"
            />
            <input
              required
              placeholder="Address line 1"
              value={form.shippingAddressLine1}
              onChange={(e) => setForm({ ...form, shippingAddressLine1: e.target.value })}
              className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <input
              placeholder="Address line 2 (optional)"
              value={form.shippingAddressLine2}
              onChange={(e) => setForm({ ...form, shippingAddressLine2: e.target.value })}
              className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <input
              required
              placeholder="City"
              value={form.shippingCity}
              onChange={(e) => setForm({ ...form, shippingCity: e.target.value })}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <input
              required
              placeholder="State"
              value={form.shippingState}
              onChange={(e) => setForm({ ...form, shippingState: e.target.value })}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <input
              required
              placeholder="Postal code"
              value={form.shippingPostalCode}
              onChange={(e) => setForm({ ...form, shippingPostalCode: e.target.value })}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
            <input
              required
              placeholder="Country"
              value={form.shippingCountry}
              onChange={(e) => setForm({ ...form, shippingCountry: e.target.value })}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>

          {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

          <button
            type="submit"
            disabled={placing}
            className="mt-6 w-full rounded-md bg-brand-600 py-2.5 text-sm font-medium text-white hover:bg-brand-700 disabled:bg-gray-300"
          >
            {placing ? 'Placing order...' : 'Place Order'}
          </button>
        </form>

        <div className="rounded-lg border border-gray-200 bg-white p-4">
          <h2 className="text-lg font-semibold text-gray-800">Order Summary</h2>
          <div className="mt-3 space-y-2 text-sm">
            {cart.items.map((item) => (
              <div key={item.productId} className="flex justify-between">
                <span className="text-gray-600">
                  {item.productName} × {item.quantity}
                </span>
                <span className="text-gray-900">{formatCurrency(item.subtotal)}</span>
              </div>
            ))}
          </div>
          <div className="mt-4 flex justify-between border-t border-gray-200 pt-3 text-base font-semibold">
            <span>Total</span>
            <span>{formatCurrency(cart.total)}</span>
          </div>
        </div>
      </div>

      {placing && <Loader label="Placing your order..." />}
    </div>
  )
}
