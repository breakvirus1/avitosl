import React from 'react'
import { useAuth } from "react-oidc-context"

function AuthBar() {
    const auth = useAuth()

    const containerStyle = {
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
    }

    const textStyle = {
        color: 'rgba(255, 255, 255, 0.85)',
        fontSize: '14px'
    }

    const buttonStyle = {
        background: '#1890ff',
        color: 'white',
        border: 'none',
        padding: '4px 12px',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '12px'
    }

    return (
        <div style={containerStyle}>
            <span style={textStyle}>
                Hi {auth.user?.profile.preferred_username}
            </span>
            <button
                style={buttonStyle}
                onClick={() => auth.signoutRedirect()}
            >
                Logout
            </button>
        </div>
    )
}

export default AuthBar
