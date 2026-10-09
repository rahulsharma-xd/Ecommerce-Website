package com.ecommerce.app.dto;

public class DeleteResponse {
    private Long id;
    private String message;
    private boolean deleted;

    public DeleteResponse(Long id, String message, boolean deleted) {
        this.id = id;
        this.message = message;
        this.deleted = deleted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
