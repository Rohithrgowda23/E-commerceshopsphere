export default function ErrorMessage({ message, onRetry }) {
  if (!message) return null
  return (
    <div className="mx-auto my-8 max-w-lg rounded-lg border border-red-200 bg-red-50 p-4 text-center">
      <p className="text-sm text-red-700">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-3 rounded-md bg-red-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-red-700"
        >
          Try again
        </button>
      )}
    </div>
  )
}
