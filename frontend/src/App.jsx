import { useState } from 'react'
import Login from './Login'
import DeveloperDashboard from './DeveloperDashboard'
import AdminDashboard from './AdminDashboard'
import './index.css'

function App() {
  // Token is kept in memory only (React state), not localStorage,
  // to reduce XSS token-theft exposure. This means a page refresh
  // logs the user out — an accepted trade-off for this project;
  // a production system would use httpOnly cookies instead.
  const [token, setToken] = useState(null)
  const [role, setRole] = useState(null)
  const [username, setUsername] = useState(null)
  const [apiKey, setApiKey] = useState(null)

  const handleLoginSuccess = (jwtToken) => {
    setToken(jwtToken)
    
    try {
      // Decode JWT to get role, username, apiKey
      const base64Url = jwtToken.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      }).join(''))
      
      const claims = JSON.parse(jsonPayload)
      setRole(claims.role)
      setUsername(claims.sub)
      setApiKey(claims.apiKey)
    } catch (e) {
      console.error("Failed to decode token", e)
    }
  }

  const handleLogout = () => {
    setToken(null)
    setRole(null)
    setUsername(null)
    setApiKey(null)
  }

  if (!token) {
    return <Login onLoginSuccess={handleLoginSuccess} />
  }

  return (
    <div>
      <div className="topbar">
        <div className="topbar-left">LLM_Gateway</div>
        <div className="topbar-right">
          <span>{username} ({role})</span>
          <button className="btn" onClick={handleLogout}>Logout</button>
        </div>
      </div>
      
      {role === 'DEVELOPER' && <DeveloperDashboard token={token} apiKey={apiKey} />}
      {role === 'ADMIN' && <AdminDashboard token={token} />}
    </div>
  )
}

export default App
