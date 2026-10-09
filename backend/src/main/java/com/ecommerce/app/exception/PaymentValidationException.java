package com.ecommerce.app.exception;

/**
 * Exception thrown when payment card validation fails.
 * Used for invalid card format, expired cards, or invalid CVV.
 */
public class PaymentValidationException extends RuntimeException {

    public PaymentValidationException(String message) {
        super(message);
    }

    public PaymentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
