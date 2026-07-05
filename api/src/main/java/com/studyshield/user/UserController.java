package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/signup")
    public ResponseEntity<SessionResponse> registerUser(
            @RequestBody SignUpRequest signUpRequest) {
        
        try {
            if (signUpRequest.getLoginId() == null || signUpRequest.getLoginId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SessionResponse(null, null, null, 
                                "Enter valid email or phone (+[country_code][number])", 
                                "INVALID_FORMAT"));
            }
            
            if (signUpRequest.getPassword() == null || signUpRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SessionResponse(null, null, null, 
                                "Password must be at least 6 characters", 
                                "WEAK_PASSWORD"));
            }

            if (signUpRequest.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new SessionResponse(null, null, null, 
                                "Password must be at least 6 characters", 
                                "WEAK_PASSWORD"));
            }
            
            // Register user with sign up logic
            SessionResponse response = userService.registerUser(
                    signUpRequest.getLoginId(), 
                    signUpRequest.getPassword());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RegistrationException e) {
            if ("EMAIL_PHONE_EXISTS".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new SessionResponse(null, null, null, 
                                "Email or phone already registered", 
                                "EMAIL_PHONE_EXISTS"));
            } else if ("WEAK_PASSWORD".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new SessionResponse(null, null, null, 
                                "Password must be at least 6 characters", 
                                "WEAK_PASSWORD"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SessionResponse(null, null, null, 
                            e.getMessage(), 
                            e.getErrorCode()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse(null, null, null, 
                            "Sign up failed", 
                            "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<SessionResponse> authenticateUser(
            @RequestBody SignInRequest signInRequest) {

        try {
            if (signInRequest.getLoginId() == null || signInRequest.getLoginId().isEmpty()
                    || signInRequest.getPassword() == null || signInRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SessionResponse(null, null, null,
                                "Email/phone and password required",
                                "MISSING_FIELDS"));
            }

            SessionResponse response = userService.authenticateUser(
                    signInRequest.getLoginId(),
                    signInRequest.getPassword());

            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            if ("INVALID_CREDENTIALS".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(null, null, null,
                                "Invalid email/phone or password",
                                "INVALID_CREDENTIALS"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse(null, null, null,
                            "Sign in failed",
                            "INTERNAL_ERROR"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse(null, null, null,
                            "Sign in failed",
                            "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) SignOutRequest signOutRequest) {

        try {
            UUID sessionId = null;

            // Extract sessionId from Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    sessionId = UUID.fromString(authHeader.substring(7));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new SessionResponse(null, null, null,
                                    "Invalid session ID format",
                                    "MISSING_SESSION_ID"));
                }
            }

            // Fall back to request body
            if (sessionId == null && signOutRequest != null && signOutRequest.getSessionId() != null) {
                sessionId = signOutRequest.getSessionId();
            }

            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new SessionResponse(null, null, null,
                                "Session ID required",
                                "MISSING_SESSION_ID"));
            }

            userService.signOut(sessionId);

            return ResponseEntity.ok(new SessionResponse(null, null, null,
                    "Sign out successful",
                    null));
        } catch (RegistrationException e) {
            if ("INVALID_SESSION".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(null, null, null,
                                "Invalid or expired session",
                                "INVALID_SESSION"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse(null, null, null,
                            "Sign out failed",
                            "INTERNAL_ERROR"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse(null, null, null,
                            "Sign out failed",
                            "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateSession(
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ValidationResponse(null, null, false,
                                "Invalid Authorization header format",
                                "BAD_HEADER"));
            }

            UUID sessionId;
            try {
                sessionId = UUID.fromString(authHeader.substring(7).trim());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ValidationResponse(null, null, false,
                                "Invalid Authorization header format",
                                "BAD_HEADER"));
            }

            ValidationResponse response = userService.validateSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            if ("INVALID_SESSION".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ValidationResponse(null, null, false,
                                "Session expired or invalid",
                                "INVALID_SESSION"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(null, null, false,
                            "Validation failed",
                            "INTERNAL_ERROR"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(null, null, false,
                            "Validation failed",
                            "INTERNAL_ERROR"));
        }
    }
}