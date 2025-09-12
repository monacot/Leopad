package com.leopad.notepad.service;

import com.leopad.notepad.entity.User;
import com.leopad.notepad.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(String email, String name) {
        User user = new User(email, name);
        return userRepository.save(user);
    }

    public User findOrCreateUser(String email, String name) {
        Optional<User> existingUser = findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        return createUser(email, name);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByFirebaseUid(String firebaseUid) {
        Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);
        return user.orElse(null);
    }

    public User createUserWithFirebaseUid(String firebaseUid, String email, String name) {
        logger.info("Creating new user with Firebase UID: {}, email: {}", firebaseUid, email);
        User user = new User(email, name);
        user.setFirebaseUid(firebaseUid);
        return userRepository.save(user);
    }

    public User findOrCreateUserByFirebaseUid(String firebaseUid, String email, String name) {
        logger.debug("Finding or creating user with Firebase UID: {}", firebaseUid);
        
        // First try to find by Firebase UID
        Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            logger.debug("Found existing user with Firebase UID: {}", firebaseUid);
            return existingUser.get();
        }

        // If not found by Firebase UID, try by email (for users created before Firebase)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            logger.info("Found existing user by email, updating with Firebase UID: {}", firebaseUid);
            User user = userByEmail.get();
            user.setFirebaseUid(firebaseUid);
            if (name != null && !name.isEmpty()) {
                user.setName(name);
            }
            return userRepository.save(user);
        }

        // Create new user
        logger.info("Creating new user with Firebase UID: {}", firebaseUid);
        return createUserWithFirebaseUid(firebaseUid, email, name);
    }
}