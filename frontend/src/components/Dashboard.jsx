import { useState, useEffect } from 'react'
import { signOut } from 'firebase/auth'
import { auth } from '../config/firebase'
import apiService from '../services/apiService'

function Dashboard({ user }) {
  const [notes, setNotes] = useState([])
  const [selectedNote, setSelectedNote] = useState(null)
  const [noteTitle, setNoteTitle] = useState('')
  const [noteContent, setNoteContent] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [isFavorite, setIsFavorite] = useState(false)
  const [showingFavorites, setShowingFavorites] = useState(false)

  useEffect(() => {
    loadNotes()
  }, [])

  const loadNotes = async () => {
    try {
      setLoading(true)
      const fetchedNotes = await apiService.getNotes()
      setNotes(fetchedNotes)
      setShowingFavorites(false)
    } catch (error) {
      setError('Failed to load notes: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const loadFavoriteNotes = async () => {
    try {
      setLoading(true)
      const favoriteNotes = await apiService.getFavoriteNotes()
      setNotes(favoriteNotes)
      setShowingFavorites(true)
    } catch (error) {
      setError('Failed to load favorite notes: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleToggleFavorites = async () => {
    if (showingFavorites) {
      await loadNotes()
    } else {
      await loadFavoriteNotes()
    }
  }

  const handleSignOut = async () => {
    try {
      await signOut(auth)
    } catch (error) {
      setError('Failed to sign out: ' + error.message)
    }
  }

  const handleNoteSelect = (note) => {
    setSelectedNote(note)
    setNoteTitle(note.title)
    setNoteContent(note.content)
    setIsFavorite(note.isFavorite || false)
  }

  const handleNewNote = () => {
    setSelectedNote(null)
    setNoteTitle('')
    setNoteContent('')
    setIsFavorite(false)
  }

  const handleSave = async () => {
    if (!noteTitle.trim()) {
      setError('Please enter a title for your note')
      return
    }

    try {
      setSaving(true)
      setError('')
      
      const noteData = {
        title: noteTitle,
        content: noteContent,
        isFavorite: isFavorite
      }

      if (selectedNote) {
        // Update existing note
        const updatedNote = await apiService.updateNote(selectedNote.id, noteData)
        setNotes(notes.map(note => 
          note.id === selectedNote.id ? updatedNote : note
        ))
        setSelectedNote(updatedNote)
        setIsFavorite(updatedNote.isFavorite || false)
      } else {
        // Create new note
        const newNote = await apiService.createNote(noteData)
        setNotes([newNote, ...notes])
        setSelectedNote(newNote)
        setIsFavorite(newNote.isFavorite || false)
      }
    } catch (error) {
      setError('Failed to save note: ' + error.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!selectedNote) return
    
    if (!confirm('Are you sure you want to delete this note?')) {
      return
    }

    try {
      setSaving(true)
      await apiService.deleteNote(selectedNote.id)
      setNotes(notes.filter(note => note.id !== selectedNote.id))
      handleNewNote()
    } catch (error) {
      setError('Failed to delete note: ' + error.message)
    } finally {
      setSaving(false)
    }
  }

  const handleEmailNote = async () => {
    if (!selectedNote) {
      setError('Please save the note first')
      return
    }

    const email = prompt('Enter email address to send this note:')
    if (!email) return

    try {
      setSaving(true)
      await apiService.emailNote(selectedNote.id, email)
      alert('Note sent successfully!')
    } catch (error) {
      setError('Failed to send note: ' + error.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <div className="loading">Loading notes...</div>
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <button onClick={handleSignOut} className="sign-out-button">
          Sign Out
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="dashboard-content">
        <div className="notes-sidebar">
          <div className="sidebar-header">
            <h3>{showingFavorites ? 'Favorite Notes' : 'Your Notes'}</h3>
            <div className="sidebar-actions">
              <button onClick={handleNewNote} className="new-note-button">
                + New Note
              </button>
              <button 
                onClick={handleToggleFavorites} 
                className={`favorites-button ${showingFavorites ? 'active' : ''}`}
                title={showingFavorites ? 'Show all notes' : 'Show favorites only'}
              >
                {showingFavorites ? 'All Notes' : 'Favorites'}
              </button>
            </div>
          </div>
          
          <div className="notes-list">
            {notes.length === 0 ? (
              <p className="no-notes">
                {showingFavorites ? 'No favorite notes yet. Star some notes to see them here!' : 'No notes yet. Create your first note!'}
              </p>
            ) : (
              notes.map(note => (
                <div
                  key={note.id}
                  className={`note-item ${selectedNote?.id === note.id ? 'selected' : ''}`}
                  onClick={() => handleNoteSelect(note)}
                >
                  <div className="note-item-header">
                    <h4>{note.title}</h4>
                    {note.isFavorite && <span className="favorite-star">★</span>}
                  </div>
                  <p>{note.content.substring(0, 100)}...</p>
                  <small>{new Date(note.createdAt).toLocaleDateString()}</small>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="note-editor">
          <div className="editor-header">
            <input
              type="text"
              placeholder="Note title..."
              value={noteTitle}
              onChange={(e) => setNoteTitle(e.target.value)}
              className="note-title-input"
            />
            
            <div className="editor-actions">
              <button
                onClick={() => setIsFavorite(!isFavorite)}
                className={`favorite-toggle ${isFavorite ? 'favorited' : ''}`}
                title={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
              >
                {isFavorite ? '★' : '☆'}
              </button>
              
              <button 
                onClick={handleSave} 
                disabled={saving}
                className="save-button"
              >
                {saving ? 'Saving...' : 'Save'}
              </button>
              
              {selectedNote && (
                <>
                  <button 
                    onClick={handleEmailNote} 
                    disabled={saving}
                    className="email-button"
                  >
                    Email
                  </button>
                  <button 
                    onClick={handleDelete} 
                    disabled={saving}
                    className="delete-button"
                  >
                    Delete
                  </button>
                </>
              )}
            </div>
          </div>

          <textarea
            placeholder="Start writing your note..."
            value={noteContent}
            onChange={(e) => setNoteContent(e.target.value)}
            className="note-content-input"
            rows={20}
          />
        </div>
      </div>
    </div>
  )
}

export default Dashboard