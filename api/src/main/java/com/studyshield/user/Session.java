package com.studyshield.user;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_sessions_session_id", columnList = "session_id"),
    @Index(name = "idx_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_sessions_is_active", columnList = "is_active"),
    @Index(name = "idx_sessions_expires_at", columnList = "expires_at")
})
public class Session {
    
    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private Long createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    // Constructors
    public Session() {}

    public Session(UUID userId, UUID sessionId, Long expiresAt) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        if (createdAt == null) createdAt = now;
        if (isActive == null) isActive = true;
    }

    // Getters and setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}