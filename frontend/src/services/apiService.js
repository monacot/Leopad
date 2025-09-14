import axios from 'axios'
import { auth } from '../config/firebase'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add request interceptor to include Firebase token
api.interceptors.request.use(
  async (config) => {
    const user = auth.currentUser
    if (user) {
      const token = await user.getIdToken()
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      const message = error.response.data?.message || error.response.data || 'An error occurred'
      throw new Error(message)
    } else if (error.request) {
      // Request was made but no response received
      throw new Error('Unable to connect to server')
    } else {
      // Something else happened
      throw new Error(error.message)
    }
  }
)

const apiService = {
  // Get all notes for the authenticated user
  async getNotes() {
    const response = await api.get('/api/notes')
    return response.data
  },

  // Create a new note
  async createNote(noteData) {
    const response = await api.post('/api/notes', noteData)
    return response.data
  },

  // Update an existing note
  async updateNote(id, noteData) {
    const response = await api.put(`/api/notes/${id}`, noteData)
    return response.data
  },

  // Delete a note
  async deleteNote(id) {
    await api.delete(`/api/notes/${id}`)
  },

  // Get favorite notes for the authenticated user
  async getFavoriteNotes() {
    const response = await api.get('/api/notes/favorites')
    return response.data
  },

  // Email a note (sends to authenticated user's email)
  async emailNote(id) {
    const response = await api.post(`/api/notes/${id}/send-email`)
    return response.data
  },

  // Health check
  async healthCheck() {
    const response = await api.get('/health')
    return response.data
  },

  // Verify token (for debugging)
  async verifyToken() {
    const response = await api.post('/api/auth/verify-token')
    return response.data
  }
}

export default apiService