package com.studyshield.user;

public class RegistrationException extends RuntimeException {
    private final String errorCode;

    public RegistrationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}