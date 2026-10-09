package com.ecommerce.app.dto;

public class UnlockResponse {
    private Long userId;
    private String email;
    private String message;
    private Boolean success;

    public UnlockResponse() {}

    public UnlockResponse(Long userId, String email, String message, Boolean success) {
        this.userId = userId;
        this.email = email;
        this.message = message;
        this.success = success;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }


}
