package com.studyshield.user;

import java.util.UUID;

public class ParentResponse {

    private UUID parentId;
    private UUID accountId;
    private String name;

    public ParentResponse() {}

    public ParentResponse(UUID parentId, UUID accountId, String name) {
        this.parentId = parentId;
        this.accountId = accountId;
        this.name = name;
    }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
