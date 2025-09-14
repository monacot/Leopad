package com.leopad.notepad.controller;

import com.leopad.notepad.dto.NoteRequest;
import com.leopad.notepad.dto.NoteResponse;
import com.leopad.notepad.entity.Note;
import com.leopad.notepad.entity.User;
import com.leopad.notepad.service.EmailService;
import com.leopad.notepad.service.NoteService;
import com.leopad.notepad.service.UserService;
import com.leopad.notepad.service.FirebaseAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.google.firebase.auth.FirebaseToken;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String firebaseUid = userDetails.getUsername(); // This is the Firebase UID
        
        // Get the Firebase token from the authentication credentials
        FirebaseToken firebaseToken = (FirebaseToken) authentication.getCredentials();
        
        logger.debug("Getting current user for Firebase UID: {} with email: {}", firebaseUid, firebaseToken.getEmail());
        
        try {
            // First try to find existing user by Firebase UID
            User user = userService.findByFirebaseUid(firebaseUid);
            if (user != null) {
                logger.debug("Found existing user: {}", user.getEmail());
                return user;
            }
            
            // If user doesn't exist, create it using Firebase token data
            String email = firebaseToken.getEmail();
            String name = firebaseToken.getName();
            if (name == null || name.isEmpty()) {
                name = email.split("@")[0]; // Use email prefix as default name
            }
            
            logger.info("Creating new user from Firebase token - UID: {}, email: {}, name: {}", firebaseUid, email, name);
            user = userService.findOrCreateUserByFirebaseUid(firebaseUid, email, name);
            
            return user;
            
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get current user", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        User user = getCurrentUser();
        logger.info("Fetching all notes for user: {}", user.getEmail());
        List<Note> notes = noteService.findAllByUser(user);
        logger.debug("Found {} notes for user: {}", notes.size(), user.getEmail());
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        User user = getCurrentUser();
        Optional<Note> note = noteService.findByIdAndUser(id, user);
        
        if (note.isPresent()) {
            return ResponseEntity.ok(new NoteResponse(note.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        User user = getCurrentUser();
        logger.info("Creating new note for user: {} with title: '{}'", user.getEmail(), request.getTitle());
        Note note = noteService.createNote(request, user);
        logger.info("Successfully created note with ID: {} for user: {}", note.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new NoteResponse(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        try {
            User user = getCurrentUser();
            Note note = noteService.updateNote(id, request, user);
            return ResponseEntity.ok(new NoteResponse(note));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            noteService.deleteNote(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam String keyword) {
        User user = getCurrentUser();
        List<Note> notes = noteService.searchNotes(keyword, user);
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<NoteResponse>> getFavoriteNotes() {
        User user = getCurrentUser();
        List<Note> notes = noteService.findFavoritesByUser(user);
        List<NoteResponse> response = notes.stream()
                .map(NoteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getUserStats() {
        User user = getCurrentUser();
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
            User user = getCurrentUser();
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