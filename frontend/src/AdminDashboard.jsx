import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import StatBlock from './StatBlock'
import DeveloperDashboard from './DeveloperDashboard'

export default function AdminDashboard({ token }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  const [lookupKey, setLookupKey] = useState('')
  const [activeLookupKey, setActiveLookupKey] = useState(null)

  useEffect(() => {
    async function fetchSummary() {
      try {
        const response = await fetch('/api/usage/summary', {
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
        setError("Couldn't load usage summary right now. Try again shortly.")
      } finally {
        setLoading(false)
      }
    }
    
    fetchSummary()
  }, [token])

  const handleLookup = (e) => {
    e.preventDefault()
    if (lookupKey.trim()) {
      setActiveLookupKey(lookupKey.trim())
    }
  }

  if (loading) {
    return <div className="container">Loading summary data...</div>
  }

  if (error) {
    return <div className="container"><div className="error-message">{error}</div></div>
  }

  if (!data) return null

  // Format chart data based on provider breakdown
  const chartData = data.providerBreakdown ? Object.keys(data.providerBreakdown).map(provider => ({
    name: provider,
    requests: data.providerBreakdown[provider]
  })) : []

  return (
    <div className="container" style={{animation: 'fadeIn 0.3s ease-in-out'}}>
      <h2 style={{marginBottom: '2rem'}}>System Overview</h2>
      
      <div className="stat-grid">
        <StatBlock title="Total System Requests" value={(data.totalRequests || 0).toLocaleString()} />
        <StatBlock title="Total System Cost" value={`$${(data.totalCost || 0).toFixed(4)}`} />
      </div>

      <div className="chart-container">
        <h3 className="chart-title">Requests by Provider</h3>
        {chartData.length > 0 ? (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
              <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: 'var(--color-text-muted)', fontFamily: 'IBM Plex Sans'}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: 'var(--color-text-muted)', fontFamily: 'IBM Plex Mono'}} />
                <Tooltip 
                  cursor={{fill: 'var(--color-bg)'}}
                  contentStyle={{
                    backgroundColor: 'var(--color-surface)',
                    border: '1px solid var(--color-border)',
                    borderRadius: '0',
                    fontFamily: 'IBM Plex Sans'
                  }}
                  itemStyle={{fontFamily: 'IBM Plex Mono', color: 'var(--color-text)'}}
                />
                <Bar dataKey="requests" fill="var(--color-accent)" radius={[0, 0, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <p style={{color: 'var(--color-text-muted)'}}>No provider data available.</p>
        )}
      </div>

      <div style={{marginTop: '3rem'}}>
        <h2 style={{marginBottom: '1.5rem'}}>Lookup Developer Usage</h2>
        <form onSubmit={handleLookup} className="lookup-form">
          <input 
            type="text" 
            placeholder="Enter API Key to lookup..." 
            value={lookupKey}
            onChange={(e) => setLookupKey(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Lookup</button>
        </form>
        
        {activeLookupKey && (
          <div style={{marginTop: '2rem', borderTop: '1px solid var(--color-border)', paddingTop: '2rem'}}>
            <h3 style={{marginBottom: '1rem'}}>Usage for key: <span className="mono">{activeLookupKey}</span></h3>
            <DeveloperDashboard token={token} apiKey={activeLookupKey} />
          </div>
        )}
      </div>
    </div>
  )
}
