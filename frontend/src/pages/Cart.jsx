import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { formatCurrency } from '../utils/format'
import Loader from '../components/common/Loader'
import CartItemRow from '../components/cart/CartItemRow'

export default function Cart() {
    const { cart, loading, updateItem, removeItem, clearCart } = useCart()
    const navigate = useNavigate()

    if (loading) return <Loader label="Loading your cart..." />

    const items = cart.items || []

    if (items.length === 0) {
        return (
            <div className="mx-auto max-w-3xl px-4 py-16 text-center">
                <h1 className="text-2xl font-bold text-gray-900">Your cart is empty</h1>
                <p className="mt-2 text-gray-500">Browse our products and add something you like.</p>
                <Link
                    to="/products"
                    className="mt-6 inline-block rounded-md bg-brand-600 px-6 py-2 text-sm font-medium text-white hover:bg-brand-700"
                >
                    Shop Now
                </Link>
            </div>
        )
    }

    return (
        <div className="mx-auto max-w-4xl px-4 py-8">
            <h1 className="text-2xl font-bold text-gray-900">Your Cart</h1>

            <div className="mt-6 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
                {items.map((item) => (
                    <CartItemRow
                        key={item.productId}
                        item={item}
                        onUpdateQuantity={updateItem}
                        onRemove={removeItem}
                    />
                ))}
            </div>

            <div className="mt-6 flex items-center justify-between">
                <button onClick={clearCart} className="text-sm text-gray-500 hover:text-red-600">
                    Clear cart
                </button>

                <div className="text-right">
                    <p className="text-sm text-gray-500">Total</p>
                    <p className="text-2xl font-bold text-gray-900">{formatCurrency(cart.total)}</p>
                    <button
                        onClick={() => navigate('/checkout')}
                        className="mt-3 rounded-md bg-brand-600 px-6 py-2 text-sm font-medium text-white hover:bg-brand-700"
                    >
                        Proceed to Checkout
                    </button>
                </div>
            </div>
        </div>
    )
}