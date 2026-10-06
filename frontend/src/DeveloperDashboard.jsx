import { useState, useEffect } from 'react'
import StatBlock from './StatBlock'

export default function DeveloperDashboard({ token, apiKey }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function fetchData() {
      try {
        const response = await fetch(`/api/usage/${apiKey}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        
        const result = await response.json()
        setData(result)
      } catch (err) {
        setError("Couldn't load your usage right now. Try again shortly.")
      } finally {
        setLoading(false)
      }
    }
    
    if (apiKey) {
      fetchData()
    }
  }, [apiKey, token])

  if (loading) {
    return <div className="container">Loading your data...</div>
  }

  if (error) {
    return <div className="container"><div className="error-message">{error}</div></div>
  }

  if (!data) return null
  
  // Calculate aggregate stats for the developer
  const totalRequests = data.requests ? data.requests.length : 0
  const totalCost = data.requests ? data.requests.reduce((sum, req) => sum + (req.cost || 0), 0) : 0
  const totalInputTokens = data.requests ? data.requests.reduce((sum, req) => sum + (req.inputTokens || 0), 0) : 0
  const totalOutputTokens = data.requests ? data.requests.reduce((sum, req) => sum + (req.outputTokens || 0), 0) : 0
  const hasFailover = data.requests ? data.requests.some(req => req.wasFailover) : false

  return (
    <div className="container" style={{animation: 'fadeIn 0.3s ease-in-out'}}>
      <h2 style={{marginBottom: '2rem'}}>Usage Overview</h2>
      
      <div className="stat-grid">
        <StatBlock title="Total Requests" value={totalRequests.toLocaleString()} showFailover={hasFailover} />
        <StatBlock title="Total Cost" value={`$${totalCost.toFixed(4)}`} />
        <StatBlock title="Input Tokens" value={totalInputTokens.toLocaleString()} />
        <StatBlock title="Output Tokens" value={totalOutputTokens.toLocaleString()} />
      </div>
    </div>
  )
}
