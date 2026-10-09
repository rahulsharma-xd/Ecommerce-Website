package com.ecommerce.app.service;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.exception.PaymentValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Payment service for fake payment processing and card validation.
 * Validates card details but does not store them or process real payments.
 */
@Service
public class PaymentService {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    /**
     * Validate credit card details format
     * Checks card number, expiry date, and CVV
     *
     * @param request checkout request with card details
     * @throws PaymentValidationException if validation fails
     */
    public void validateCardDetails(CheckoutRequest request) {
        // Validate card number
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new PaymentValidationException("Card number is required");
        }

        String cardNumber = request.getCardNumber().replaceAll("\\s", ""); // Remove spaces
        if (!cardNumber.matches("\\d{13,19}")) {
            throw new PaymentValidationException("Card number must be 13-19 digits");
        }

        // Validate using Luhn algorithm
        if (!isValidLuhn(cardNumber)) {
            throw new PaymentValidationException("Invalid card number (failed Luhn check)");
        }

        // Validate card holder name
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw new PaymentValidationException("Card holder name is required");
        }

        // Validate expiry date
        if (request.getExpiryDate() == null || request.getExpiryDate().trim().isEmpty()) {
            throw new PaymentValidationException("Expiry date is required");
        }

        if (!isValidExpiryDate(request.getExpiryDate())) {
            throw new PaymentValidationException("Invalid or expired card. Expiry date must be in MM/YY format and not expired");
        }

        // Validate CVV
        if (request.getCvv() == null || request.getCvv().trim().isEmpty()) {
            throw new PaymentValidationException("CVV is required");
        }

        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new PaymentValidationException("CVV must be 3-4 digits");
        }
    }

    /**
     * Process fake payment (always succeeds for demo purposes)
     * In production, this would integrate with a real payment gateway
     *
     * @param amount the payment amount
     * @param request checkout request with card details
     * @return true if payment successful (always true for demo)
     */
    public boolean processFakePayment(BigDecimal amount, CheckoutRequest request) {
        // Fake payment processing - always succeeds
        // Could add fake failure for testing with specific card numbers
        // e.g., if card number is "0000000000000000", return false

        String cardNumber = request.getCardNumber().replaceAll("\\s", "");

        // Simulate payment failure for testing (card number = 0000000000000000)
        if (cardNumber.equals("0000000000000000")) {
            throw new PaymentValidationException("Payment declined by bank");
        }

        // Log fake payment (in production, this would call payment gateway)
        System.out.println("Processing fake payment:");
        System.out.println("  Amount: $" + amount);
        System.out.println("  Card: **** **** **** " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("  Status: SUCCESS (Fake Payment)");

        return true;
    }

    /**
     * Validate card number using Luhn algorithm
     * https://en.wikipedia.org/wiki/Luhn_algorithm
     *
     * @param cardNumber the card number to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Validate expiry date format and ensure card is not expired
     *
     * @param expiryDate expiry date in MM/YY format
     * @return true if valid and not expired, false otherwise
     */
    private boolean isValidExpiryDate(String expiryDate) {
        try {
            // Parse expiry date
            YearMonth expiry = YearMonth.parse(expiryDate, EXPIRY_FORMATTER);
            YearMonth now = YearMonth.now();

            // Check if expired (must be current month or future)
            return !expiry.isBefore(now);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
