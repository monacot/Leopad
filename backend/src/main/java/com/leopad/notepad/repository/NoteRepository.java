package com.leopad.notepad.repository;

import com.leopad.notepad.entity.Note;
import com.leopad.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    
    List<Note> findByUserAndIsFavoriteOrderByCreatedAtDesc(User user, Boolean isFavorite);
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY n.createdAt DESC")
    List<Note> findByUserAndTitleContainingOrContentContaining(@Param("user") User user, @Param("keyword") String keyword);
    
    Optional<Note> findByIdAndUser(Long id, User user);
}