package com.ecommerce.app.exception;

/**
 * Exception thrown when product stock is insufficient for checkout.
 * Used when requested quantity exceeds available stock.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
