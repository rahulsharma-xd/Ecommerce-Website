package com.ecommerce.app.dto;

public class UnlockRequest {
    private String email;

    public UnlockRequest() {}

    public UnlockRequest(String email) {
    this.email = email;
    }

    // getters, setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
