package com.leopad.notepad.service;

import com.leopad.notepad.dto.NoteRequest;
import com.leopad.notepad.entity.Note;
import com.leopad.notepad.entity.User;
import com.leopad.notepad.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public List<Note> findAllByUser(User user) {
        return noteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Note> findByIdAndUser(Long id, User user) {
        return noteRepository.findByIdAndUser(id, user);
    }

    public Note createNote(NoteRequest request, User user) {
        Note note = new Note(request.getTitle(), request.getContent(), user);
        if (request.getIsFavorite() != null) {
            note.setIsFavorite(request.getIsFavorite());
        }
        return noteRepository.save(note);
    }

    public Note updateNote(Long id, NoteRequest request, User user) {
        Optional<Note> noteOpt = findByIdAndUser(id, user);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or access denied");
        }

        Note note = noteOpt.get();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        if (request.getIsFavorite() != null) {
            note.setIsFavorite(request.getIsFavorite());
        }
        
        return noteRepository.save(note);
    }

    public void deleteNote(Long id, User user) {
        Optional<Note> noteOpt = findByIdAndUser(id, user);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or access denied");
        }
        
        noteRepository.delete(noteOpt.get());
    }

    public List<Note> searchNotes(String keyword, User user) {
        return noteRepository.findByUserAndTitleContainingOrContentContaining(user, keyword);
    }

    public List<Note> findFavoritesByUser(User user) {
        return noteRepository.findByUserAndIsFavoriteOrderByCreatedAtDesc(user, true);
    }

    public long countByUser(User user) {
        return noteRepository.countByUser(user);
    }
}