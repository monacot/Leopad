import { useState } from 'react'
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signInWithPopup, 
  signOut 
} from 'firebase/auth'
import { auth, googleProvider } from '../config/firebase'

function Auth() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isLogin, setIsLogin] = useState(true)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (isLogin) {
        await signInWithEmailAndPassword(auth, email, password)
      } else {
        await createUserWithEmailAndPassword(auth, email, password)
      }
    } catch (error) {
      setError(error.message)
    }
    setLoading(false)
  }

  const handleGoogleSignIn = async () => {
    setError('')
    setLoading(true)
    
    try {
      await signInWithPopup(auth, googleProvider)
    } catch (error) {
      setError(error.message)
    }
    setLoading(false)
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>{isLogin ? 'Sign In' : 'Sign Up'}</h2>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password:</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              disabled={loading}
            />
          </div>
          
          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? 'Loading...' : (isLogin ? 'Sign In' : 'Sign Up')}
          </button>
        </form>
        
        <div className="auth-divider">
          <span>or</span>
        </div>
        
        <button 
          onClick={handleGoogleSignIn} 
          className="google-button"
          disabled={loading}
        >
          {loading ? 'Loading...' : 'Sign in with Google'}
        </button>
        
        <div className="auth-toggle">
          <p>
            {isLogin ? "Don't have an account? " : "Already have an account? "}
            <button 
              type="button"
              onClick={() => setIsLogin(!isLogin)}
              className="toggle-button"
            >
              {isLogin ? 'Sign Up' : 'Sign In'}
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Auth