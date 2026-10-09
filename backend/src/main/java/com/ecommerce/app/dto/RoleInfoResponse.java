package com.ecommerce.app.dto;

import java.util.Set;

public class RoleInfoResponse {
    private String email;
    private Set<String> authorities;
    private boolean isAdmin;
    private boolean isUser;

    public RoleInfoResponse(String email, Set<String> authorities, boolean isAdmin, boolean isUser) {
        this.email = email;
        this.authorities = authorities;
        this.isAdmin = isAdmin;
        this.isUser = isUser;
    }

    // Getters
    public String getEmail() { return email; }
    public Set<String> getAuthorities() { return authorities; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isUser() { return isUser; }
}
