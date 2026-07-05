package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody SignUpRequest request) {
        try {
            if (request.getLoginId() == null || request.getLoginId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Enter valid email or phone (+[country_code][number])", "INVALID_FORMAT"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty() || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Password must be at least 6 characters", "WEAK_PASSWORD"));
            }
            AuthResponse response = accountService.registerUser(
                    request.getLoginId(), request.getPassword(), request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RegistrationException e) {
            if ("EMAIL_PHONE_EXISTS".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new AuthResponse("Email or phone already registered", "EMAIL_PHONE_EXISTS"));
            }
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(e.getMessage(), e.getErrorCode()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Sign up failed", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody SignInRequest request) {
        try {
            if (request.getLoginId() == null || request.getLoginId().isEmpty()
                    || request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Email/phone and password required", "MISSING_FIELDS"));
            }
            AuthResponse response = accountService.authenticateUser(
                    request.getLoginId(), request.getPassword(), request.getParentId());

            if (response.isRequiresParentSelection()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email/phone or password", "INVALID_CREDENTIALS"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Sign in failed", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<AuthResponse> signOut(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) SignOutRequest signOutRequest) {
        try {
            UUID sessionId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    sessionId = UUID.fromString(authHeader.substring(7));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new AuthResponse("Invalid session ID format", "MISSING_SESSION_ID"));
                }
            }
            if (sessionId == null && signOutRequest != null && signOutRequest.getSessionId() != null) {
                sessionId = signOutRequest.getSessionId();
            }
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Session ID required", "MISSING_SESSION_ID"));
            }
            AuthResponse response = accountService.signOut(sessionId);
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid or expired session", "INVALID_SESSION"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Sign out failed", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateSession(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ValidationResponse(null, null, null, null, false,
                                "Invalid Authorization header format", "BAD_HEADER"));
            }
            UUID sessionId;
            try {
                sessionId = UUID.fromString(authHeader.substring(7).trim());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ValidationResponse(null, null, null, null, false,
                                "Invalid Authorization header format", "BAD_HEADER"));
            }
            ValidationResponse response = accountService.validateSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidationResponse(null, null, null, null, false,
                            "Session expired or invalid", "INVALID_SESSION"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(null, null, null, null, false,
                            "Validation failed", "INTERNAL_ERROR"));
        }
    }
}
