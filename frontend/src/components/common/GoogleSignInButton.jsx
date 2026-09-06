import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function GoogleSignInButton() {
    const { loginWithGoogle } = useAuth()
    const navigate = useNavigate()
    const buttonRef = useRef(null)

    useEffect(() => {
        if (!GOOGLE_CLIENT_ID) return
        let cancelled = false

        const init = () => {
            if (cancelled || !window.google?.accounts?.id || !buttonRef.current) return

            window.google.accounts.id.initialize({
                client_id: GOOGLE_CLIENT_ID,
                callback: async (response) => {
                    try {
                        await loginWithGoogle(response.credential)
                        navigate('/', { replace: true })
                    } catch {
                        // AuthContext already captured the error message for display
                    }
                },
            })

            window.google.accounts.id.renderButton(buttonRef.current, {
                theme: 'outline',
                size: 'large',
                width: 320,
            })
        }

        // The Google script tag is async — it may not be loaded yet the
        // instant this component mounts, so poll briefly until it is.
        if (window.google?.accounts?.id) {
            init()
        } else {
            const interval = setInterval(() => {
                if (window.google?.accounts?.id) {
                    clearInterval(interval)
                    init()
                }
            }, 200)
            return () => {
                cancelled = true
                clearInterval(interval)
            }
        }
    }, [loginWithGoogle, navigate])

    if (!GOOGLE_CLIENT_ID) {
        return (
            <p className="text-xs text-gray-400">
                Google sign-in isn&apos;t configured (missing VITE_GOOGLE_CLIENT_ID).
            </p>
        )
    }

    return <div ref={buttonRef} />
}