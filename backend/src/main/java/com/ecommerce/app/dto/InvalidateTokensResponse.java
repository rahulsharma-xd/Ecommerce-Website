package com.ecommerce.app.dto;

import java.time.LocalDateTime;

public class InvalidateTokensResponse {
    private String message;
    private LocalDateTime invalidatedAt;
    private boolean success;

    public InvalidateTokensResponse(String message, LocalDateTime invalidatedAt, boolean success) {
        this.message = message;
        this.invalidatedAt = invalidatedAt;
        this.success = success;
    }

    // Getters
    public String getMessage() { return message; }
    public LocalDateTime getInvalidatedAt() { return invalidatedAt; }
    public boolean isSuccess() { return success; }
}
