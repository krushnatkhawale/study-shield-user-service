package com.studyshield.user;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class SignInRequest {

    @NotBlank(message = "Email/phone is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;

    private boolean guest;

    private UUID parentId;

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isGuest() { return guest; }
    public void setGuest(boolean guest) { this.guest = guest; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
}
