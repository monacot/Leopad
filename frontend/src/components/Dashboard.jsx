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

  useEffect(() => {
    loadNotes()
  }, [])

  const loadNotes = async () => {
    try {
      setLoading(true)
      const fetchedNotes = await apiService.getNotes()
      setNotes(fetchedNotes)
    } catch (error) {
      setError('Failed to load notes: ' + error.message)
    } finally {
      setLoading(false)
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
  }

  const handleNewNote = () => {
    setSelectedNote(null)
    setNoteTitle('')
    setNoteContent('')
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
        content: noteContent
      }

      if (selectedNote) {
        // Update existing note
        const updatedNote = await apiService.updateNote(selectedNote.id, noteData)
        setNotes(notes.map(note => 
          note.id === selectedNote.id ? updatedNote : note
        ))
        setSelectedNote(updatedNote)
      } else {
        // Create new note
        const newNote = await apiService.createNote(noteData)
        setNotes([newNote, ...notes])
        setSelectedNote(newNote)
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
            <h3>Your Notes</h3>
            <button onClick={handleNewNote} className="new-note-button">
              + New Note
            </button>
          </div>
          
          <div className="notes-list">
            {notes.length === 0 ? (
              <p className="no-notes">No notes yet. Create your first note!</p>
            ) : (
              notes.map(note => (
                <div
                  key={note.id}
                  className={`note-item ${selectedNote?.id === note.id ? 'selected' : ''}`}
                  onClick={() => handleNoteSelect(note)}
                >
                  <h4>{note.title}</h4>
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