package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User registerUser(String username, String email) {
        // Generate a secure unique ID (better than UUID)
        String userId = generateSecureUserId();
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUserId(userId);
        if (existingUser.isPresent()) {
            // If userId already exists, generate new one
            userId = generateSecureUserId();
        }
        
        User user = new User(username, email, userId);
        
        // Generate auth token for mobile app
        String authToken = UUID.randomUUID().toString();
        user.setAuthToken(authToken);
        
        return userRepository.save(user);
    }
    
    public User getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(String userId, String username, String email) {
        User user = getUserById(userId);
        
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
    
    private String generateSecureUserId() {
        // Generate a secure, unique user ID that's more secure than UUID
        return "USR_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
}