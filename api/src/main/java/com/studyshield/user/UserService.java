package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    public SessionResponse registerUser(String loginId, String password) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByLoginId(loginId);
        if (existingUser.isPresent()) {
            throw new RegistrationException("Email or phone already registered", "EMAIL_PHONE_EXISTS");
        }
        
        // Validate password strength
        if (password == null || password.length() < 6) {
            throw new RegistrationException("Password must be at least 6 characters", "WEAK_PASSWORD");
        }
        
        // Create user
        User user = new User(loginId, passwordUtil.encodePassword(password));
        
        // Save user to the database
        User savedUser = userRepository.save(user);
        
        // Generate session ID (UUID)
        UUID sessionId = UUID.randomUUID();
        
        // Create session with 30-day expiry (in milliseconds)
        Long expirationTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 days
        Session session = new Session(savedUser.getId(), sessionId, expirationTime);
        sessionRepository.save(session);
        
        return new SessionResponse(
            savedUser.getId(),
            savedUser.getLoginId(),
            sessionId,
            "Sign up successful",
            null
        );
    }
    
    public SessionResponse authenticateUser(String loginId, String password) {
        // Find user by loginId
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RegistrationException("Invalid email/phone or password", "INVALID_CREDENTIALS"));
        
        // Verify password
        if (!passwordUtil.matches(password, user.getPasswordHash())) {
            throw new RegistrationException("Invalid email/phone or password", "INVALID_CREDENTIALS");
        }
        
        // Generate new session
        UUID sessionId = UUID.randomUUID();
        Long expirationTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
        Session session = new Session(user.getId(), sessionId, expirationTime);
        sessionRepository.save(session);
        
        return new SessionResponse(
            user.getId(),
            user.getLoginId(),
            sessionId,
            "Sign in successful",
            null
        );
    }
    
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    public Optional<User> getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }

    public void signOut(UUID sessionId) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));
        session.setIsActive(false);
        sessionRepository.save(session);
    }

    public ValidationResponse validateSession(UUID sessionId) {
        Session session = sessionRepository
                .findBySessionIdAndIsActiveTrueAndExpiresAtAfter(sessionId, System.currentTimeMillis())
                .orElseThrow(() -> new RegistrationException("Session expired or invalid", "INVALID_SESSION"));

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new RegistrationException("User not found", "INVALID_SESSION"));

        return new ValidationResponse(
                user.getId(),
                user.getLoginId(),
                true,
                "Session valid",
                null
        );
    }
}