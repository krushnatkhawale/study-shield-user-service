package com.studyshield.user;

import java.util.UUID;

public class ValidationResponse {

    private UUID userId;
    private String loginId;
    private boolean isValid;
    private String message;
    private String errorCode;
    private Long timestamp;

    public ValidationResponse(UUID userId, String loginId, boolean isValid, String message, String errorCode) {
        this.userId = userId;
        this.loginId = loginId;
        this.isValid = isValid;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}