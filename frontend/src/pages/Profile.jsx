import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../api/userApi'
import Loader from '../components/common/Loader'
import ErrorMessage from '../components/common/ErrorMessage'

export default function Profile() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await userApi.getProfile(user.userId)
      setProfile(data)
      setForm({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        phoneNumber: data.phoneNumber || '',
        preferredLanguage: data.preferredLanguage || 'EN',
        marketingOptIn: data.marketingOptIn || false,
      })
    } catch {
      setError('Could not load your profile.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setSaved(false)
    try {
      const updated = await userApi.updateProfile(user.userId, form)
      setProfile(updated)
      setSaved(true)
    } catch {
      setError('Could not save your changes.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <Loader />
  if (error && !profile) return <ErrorMessage message={error} onRetry={load} />

  return (
    <div className="mx-auto max-w-lg px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">My Profile</h1>
      <p className="mt-1 text-sm text-gray-500">{profile?.email}</p>

      <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="text-sm font-medium text-gray-700">First name</label>
            <input
              required
              value={form.firstName}
              onChange={(e) => setForm({ ...form, firstName: e.target.value })}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Last name</label>
            <input
              required
              value={form.lastName}
              onChange={(e) => setForm({ ...form, lastName: e.target.value })}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
        </div>

        <div>
          <label className="text-sm font-medium text-gray-700">Phone number</label>
          <input
            value={form.phoneNumber}
            onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="text-sm font-medium text-gray-700">Preferred language</label>
          <select
            value={form.preferredLanguage}
            onChange={(e) => setForm({ ...form, preferredLanguage: e.target.value })}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="EN">English</option>
            <option value="ES">Español</option>
            <option value="FR">Français</option>
            <option value="DE">Deutsch</option>
            <option value="HI">हिन्दी</option>
          </select>
        </div>

        <label className="flex items-center gap-2 text-sm text-gray-700">
          <input
            type="checkbox"
            checked={form.marketingOptIn}
            onChange={(e) => setForm({ ...form, marketingOptIn: e.target.checked })}
          />
          Send me marketing emails
        </label>

        {error && <p className="text-sm text-red-600">{error}</p>}
        {saved && <p className="text-sm text-green-600">Profile updated.</p>}

        <button
          type="submit"
          disabled={saving}
          className="rounded-md bg-brand-600 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:bg-gray-300"
        >
          {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </form>
    </div>
  )
}
