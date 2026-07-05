package com.studyshield.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<SessionResponse> handleRegistrationException(RegistrationException e) {
        HttpStatus status;
        switch (e.getErrorCode()) {
            case "EMAIL_PHONE_EXISTS":
                status = HttpStatus.CONFLICT;
                break;
            case "WEAK_PASSWORD":
            case "INVALID_FORMAT":
            case "MISSING_FIELDS":
            case "MISSING_SESSION_ID":
            case "BAD_HEADER":
                status = HttpStatus.BAD_REQUEST;
                break;
            case "INVALID_CREDENTIALS":
            case "INVALID_SESSION":
                status = HttpStatus.UNAUTHORIZED;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status)
                .body(new SessionResponse(null, null, null, e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SessionResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SessionResponse(null, null, null, "Internal server error", "INTERNAL_ERROR"));
    }
}