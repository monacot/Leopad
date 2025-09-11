package com.leopad.notepad.service;

import com.leopad.notepad.entity.User;
import com.leopad.notepad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

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
}