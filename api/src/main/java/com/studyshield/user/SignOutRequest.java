package com.studyshield.user;

import java.util.UUID;

public class SignOutRequest {

    private UUID sessionId;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}