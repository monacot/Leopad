import { useState, useEffect } from 'react'
import { auth } from './config/firebase'
import { onAuthStateChanged } from 'firebase/auth'
import Auth from './components/Auth'
import Dashboard from './components/Dashboard'
import './App.css'

function App() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user)
      setLoading(false)
    })

    return () => unsubscribe()
  }, [])

  if (loading) {
    return (
      <div className="app">
        <div className="loading">
          <h2>Loading...</h2>
        </div>
      </div>
    )
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Leopad</h1>
        {user && (
          <div className="user-info">
            <span>Welcome, {user.displayName || user.email}</span>
          </div>
        )}
      </header>

      <main className="app-main">
        {user ? <Dashboard user={user} /> : <Auth />}
      </main>
    </div>
  )
}

export default App