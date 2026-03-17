import { useAuth } from "react-oidc-context"

function PrivateRoute({ children }) {
    const auth = useAuth()

    const containerStyle = {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        textAlign: 'center'
    }

    const titleStyle = {
        fontSize: '32px',
        fontWeight: 600,
        marginBottom: '16px',
        color: '#000000d9'
    }

    const subtitleStyle = {
        fontSize: '20px',
        color: '#00000073',
        marginBottom: '24px'
    }

    const spinStyle = {
        display: 'inline-block',
        width: '40px',
        height: '40px',
        border: '4px solid #f3f3f3',
        borderTop: '4px solid #1890ff',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite'
    }

    if (auth.isLoading) {
        return (
            <div style={containerStyle}>
                <h1 style={titleStyle}>Keycloak is loading</h1>
                <h2 style={subtitleStyle}>or running authorization code flow with PKCE</h2>
                <div style={spinStyle}></div>
            </div>
        )
    }

    if (auth.error) {
        return (
            <div style={containerStyle}>
                <h1 style={titleStyle}>Oops ...</h1>
                <h2 style={subtitleStyle}>{auth.error.message}</h2>
            </div>
        )
    }

    if (!auth.isAuthenticated) {
        auth.signinRedirect()
        return null
    }

    return children
}

export default PrivateRoute
