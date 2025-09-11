package com.leopad.notepad.controller;

import com.leopad.notepad.dto.NoteRequest;
import com.leopad.notepad.dto.NoteResponse;
import com.leopad.notepad.entity.Note;
import com.leopad.notepad.entity.User;
import com.leopad.notepad.service.EmailService;
import com.leopad.notepad.service.NoteService;
import com.leopad.notepad.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // For now, we'll use a hardcoded user until authentication is implemented
    private User getDefaultUser() {
        return userService.findOrCreateUser("demo@notepad.com", "Demo User");
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        User user = getDefaultUser();
        List<Note> notes = noteService.findAllByUser(user);
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        User user = getDefaultUser();
        Optional<Note> note = noteService.findByIdAndUser(id, user);
        
        if (note.isPresent()) {
            return ResponseEntity.ok(new NoteResponse(note.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        User user = getDefaultUser();
        Note note = noteService.createNote(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new NoteResponse(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        try {
            User user = getDefaultUser();
            Note note = noteService.updateNote(id, request, user);
            return ResponseEntity.ok(new NoteResponse(note));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        try {
            User user = getDefaultUser();
            noteService.deleteNote(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam String keyword) {
        User user = getDefaultUser();
        List<Note> notes = noteService.searchNotes(keyword, user);
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<NoteResponse>> getFavoriteNotes() {
        User user = getDefaultUser();
        List<Note> notes = noteService.findFavoritesByUser(user);
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getUserStats() {
        User user = getDefaultUser();
        long totalNotesCount = noteService.countByUser(user);
        long favoriteNotesCount = noteService.findFavoritesByUser(user).size();
        
        return ResponseEntity.ok(new Object() {
            public final String userEmail = user.getEmail();
            public final long totalNotes = totalNotesCount;
            public final long favoriteNotes = favoriteNotesCount;
        });
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<Object> sendNoteByEmail(@PathVariable Long id, @RequestParam String email) {
        try {
            User user = getDefaultUser();
            Optional<Note> noteOpt = noteService.findByIdAndUser(id, user);
            
            if (noteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Note note = noteOpt.get();
            emailService.sendNoteByEmail(email, note.getTitle(), note.getContent());
            
            return ResponseEntity.ok(new Object() {
                public final String message = "Note sent successfully to " + email;
                public final Long noteId = note.getId();
                public final String noteTitle = note.getTitle();
                public final String sentTo = email;
            });
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Object() {
                public final String error = "Failed to send email";
                public final String message = e.getMessage();
            });
        }
    }
}