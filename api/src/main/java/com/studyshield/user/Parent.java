package com.studyshield.user;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "parents", indexes = {
    @Index(name = "idx_parents_account_id", columnList = "account_id")
})
public class Parent {

    @Id
    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "created_at", updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public Parent() {}

    public Parent(UUID parentId, UUID accountId, String name) {
        this.parentId = parentId;
        this.accountId = accountId;
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
