package com.ecommerce.app.exception;

/**
 * Exception thrown when order is not found or user doesn't have access.
 * Used for invalid order IDs or unauthorized access attempts.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
