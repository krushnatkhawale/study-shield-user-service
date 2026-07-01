package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        
        try {
            // At least one of username or email is required
            if ((username == null || username.isEmpty()) && (email == null || email.isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(new RegistrationResponse(null, null, "Username or email is required"));
            }
            
            User user = userService.registerUser(username, email);
            
            RegistrationResponse response = new RegistrationResponse(
                    user.getUserId(),
                    user.getAuthToken(),
                    "Registration successful"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegistrationResponse(null, null, "Registration failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        Iterable<User> users = userService.getAllUsers();
        return ResponseEntity.ok((List<User>) users);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        
        try {
            User updatedUser = userService.updateUser(userId, username, email);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

class RegistrationResponse {
    private String userId;
    private String authToken;
    private String message;
    
    public RegistrationResponse(String userId, String authToken, String message) {
        this.userId = userId;
        this.authToken = authToken;
        this.message = message;
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}