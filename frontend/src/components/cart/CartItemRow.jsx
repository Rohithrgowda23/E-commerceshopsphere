import { formatCurrency } from '../../utils/format'

export default function CartItemRow({ item, onUpdateQuantity, onRemove }) {
    return (
        <div className="flex items-center gap-4 p-4">
            <div className="h-16 w-16 shrink-0 overflow-hidden rounded-md bg-gray-100">
                {item.imageUrl && (
                    <img src={item.imageUrl} alt={item.productName} className="h-full w-full object-cover" />
                )}
            </div>

            <div className="flex-1">
                <p className="text-sm font-medium text-gray-800">{item.productName}</p>
                {!item.available && <p className="text-xs text-red-500">No longer available</p>}
                <p className="text-sm text-gray-500">{formatCurrency(item.unitPrice)} each</p>
            </div>

            <input
                type="number"
                min={1}
                value={item.quantity}
                onChange={(e) => onUpdateQuantity(item.productId, Math.max(1, Number(e.target.value)))}
                className="w-16 rounded-md border border-gray-300 px-2 py-1.5 text-sm"
            />

            <span className="w-20 text-right text-sm font-semibold text-gray-900">
        {formatCurrency(item.subtotal)}
      </span>

            <button
                onClick={() => onRemove(item.productId)}
                className="text-sm text-gray-400 hover:text-red-500"
            >
                Remove
            </button>
        </div>
    )
}