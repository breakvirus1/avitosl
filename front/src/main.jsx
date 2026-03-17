import React from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from "react-oidc-context"

const root = createRoot(document.getElementById('root'))

const oidcConfig = {
  authority: "http://localhost:14082/realms/avitorealm",
  client_id: "avitofrontend",
  redirect_uri: "http://localhost:5173",
  onSigninCallback: () => {
    window.history.replaceState(
      {},
      document.title,
      window.location.pathname
    )
  }
}

root.render(
  <React.StrictMode>
    <AuthProvider {...oidcConfig}>
      <App />
    </AuthProvider>
  </React.StrictMode>
)
