package com.studyshield.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private static final Logger log = LoggerFactory.getLogger(ParentController.class);

    @Autowired
    private ParentService parentService;

    @PostMapping
    public ResponseEntity<?> addParent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ParentRequest request) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Invalid session ID format", "MISSING_SESSION_ID"));
            }
            ParentResponse response = parentService.addParent(sessionId, request.getName());
            log.info("Parent '{}' added", response.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid or expired session", "INVALID_SESSION"));
        }
    }

    @GetMapping
    public ResponseEntity<?> listParents(
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Invalid session ID format", "MISSING_SESSION_ID"));
            }
            List<ParentResponse> parents = parentService.listParents(sessionId);
            return ResponseEntity.ok(parents);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid or expired session", "INVALID_SESSION"));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyName(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ParentRequest request) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Invalid session ID format", "MISSING_SESSION_ID"));
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Name is required", "MISSING_FIELDS"));
            }
            ParentResponse response = parentService.updateMyName(sessionId, request.getName());
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            if ("PARENT_NOT_FOUND".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse("Parent not found", "PARENT_NOT_FOUND"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid or expired session", "INVALID_SESSION"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") UUID parentId) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse("Invalid session ID format", "MISSING_SESSION_ID"));
            }
            parentService.deleteParent(sessionId, parentId);
            log.info("Parent '{}' deleted", parentId);
            return ResponseEntity.noContent().build();
        } catch (RegistrationException e) {
            if ("PARENT_NOT_FOUND".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(e.getMessage(), e.getErrorCode()));
        }
    }

    private UUID extractSessionId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return UUID.fromString(authHeader.substring(7).trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
