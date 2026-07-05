package com.studyshield.user;

import java.util.UUID;

public class ValidationResponse {

    private UUID accountId;
    private String loginId;
    private UUID parentId;
    private String parentName;
    private boolean isValid;
    private String message;
    private String errorCode;
    private Long timestamp;

    public ValidationResponse() {}

    public ValidationResponse(UUID accountId, String loginId, UUID parentId,
                              String parentName, boolean isValid, String message, String errorCode) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.parentId = parentId;
        this.parentName = parentName;
        this.isValid = isValid;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
