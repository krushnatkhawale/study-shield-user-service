package com.studyshield.user;

import java.util.List;
import java.util.UUID;

public class AuthResponse {

    private UUID accountId;
    private String loginId;
    private UUID sessionId;
    private UUID parentId;
    private String parentName;
    private boolean requiresParentSelection;
    private List<ParentSummary> parents;
    private String message;
    private String errorCode;
    private Long timestamp;

    public AuthResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    // Successful auth with single parent
    public AuthResponse(UUID accountId, String loginId, UUID sessionId,
                        UUID parentId, String parentName, String message) {
        this();
        this.accountId = accountId;
        this.loginId = loginId;
        this.sessionId = sessionId;
        this.parentId = parentId;
        this.parentName = parentName;
        this.requiresParentSelection = false;
        this.message = message;
    }

    // Parent selection required
    public AuthResponse(List<ParentSummary> parents, String message) {
        this();
        this.requiresParentSelection = true;
        this.parents = parents;
        this.message = message;
    }

    // Error response
    public AuthResponse(String message, String errorCode) {
        this();
        this.message = message;
        this.errorCode = errorCode;
    }

    public static class ParentSummary {
        private UUID parentId;
        private String parentName;

        public ParentSummary() {}
        public ParentSummary(UUID parentId, String parentName) {
            this.parentId = parentId;
            this.parentName = parentName;
        }

        public UUID getParentId() { return parentId; }
        public void setParentId(UUID parentId) { this.parentId = parentId; }
        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }
    }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public boolean isRequiresParentSelection() { return requiresParentSelection; }
    public void setRequiresParentSelection(boolean requiresParentSelection) { this.requiresParentSelection = requiresParentSelection; }
    public List<ParentSummary> getParents() { return parents; }
    public void setParents(List<ParentSummary> parents) { this.parents = parents; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
