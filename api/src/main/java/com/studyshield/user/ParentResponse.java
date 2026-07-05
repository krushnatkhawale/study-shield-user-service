package com.studyshield.user;

import java.util.UUID;

public class ParentResponse {

    private UUID parentId;
    private UUID accountId;
    private String name;
    private String parentType;

    public ParentResponse() {}

    public ParentResponse(UUID parentId, UUID accountId, String name, String parentType) {
        this.parentId = parentId;
        this.accountId = accountId;
        this.name = name;
        this.parentType = parentType;
    }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentType() { return parentType; }
    public void setParentType(String parentType) { this.parentType = parentType; }
}
