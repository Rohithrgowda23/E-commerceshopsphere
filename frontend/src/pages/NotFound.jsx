import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="mx-auto max-w-lg px-4 py-24 text-center">
      <p className="text-5xl font-bold text-brand-600">404</p>
      <h1 className="mt-3 text-xl font-semibold text-gray-900">Page not found</h1>
      <Link to="/" className="mt-6 inline-block text-brand-600 hover:underline">
        Back to home
      </Link>
    </div>
  )
}
