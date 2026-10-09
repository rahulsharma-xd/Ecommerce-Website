package com.ecommerce.app.service;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.exception.PaymentValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PaymentService.
 * Tests card validation, Luhn algorithm, and fake payment processing.
 */
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Nested
    @DisplayName("Card Number Validation Tests")
    class CardNumberValidationTests {

        @Test
        @DisplayName("Should accept valid 16-digit Visa card")
        void shouldAcceptValid16DigitVisaCard() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532015112830366");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept valid 16-digit Mastercard")
        void shouldAcceptValid16DigitMastercard() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("5425233430109903");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept valid 15-digit Amex card")
        void shouldAcceptValid15DigitAmexCard() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("374245455400126");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept valid 16-digit Discover card")
        void shouldAcceptValid16DigitDiscoverCard() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("6011000991300009");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept card number with spaces")
        void shouldAcceptCardNumberWithSpaces() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532 0151 1283 0366");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept valid 15-digit Amex as alternative length")
        void shouldAcceptValid15DigitAmexAsAlternativeLength() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("374245455400126"); // Valid 15-digit Amex

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject null card number")
        void shouldRejectNullCardNumber() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber(null);

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject empty card number")
        void shouldRejectEmptyCardNumber() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number with only whitespace")
        void shouldRejectCardNumberWithOnlyWhitespace() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("   ");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number shorter than 13 digits")
        void shouldRejectCardNumberShorterThan13Digits() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("123456789012"); // 12 digits

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number must be 13-19 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number longer than 19 digits")
        void shouldRejectCardNumberLongerThan19Digits() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("12345678901234567890"); // 20 digits

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number must be 13-19 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number with non-numeric characters")
        void shouldRejectCardNumberWithNonNumericCharacters() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532-0151-1283-0366");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number must be 13-19 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number with letters")
        void shouldRejectCardNumberWithLetters() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("453201511283O366"); // O instead of 0

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card number must be 13-19 digits", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Luhn Algorithm Validation Tests")
    class LuhnAlgorithmValidationTests {

        @Test
        @DisplayName("Should accept card number passing Luhn check")
        void shouldAcceptCardNumberPassingLuhnCheck() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532015112830366"); // Valid Luhn

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject card number failing Luhn check - one digit changed")
        void shouldRejectCardNumberFailingLuhnCheckOneDigitChanged() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532015112830365"); // Invalid Luhn (last digit changed)

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid card number (failed Luhn check)", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number with all same digits")
        void shouldRejectCardNumberWithAllSameDigits() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("1111111111111111"); // Fails Luhn

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid card number (failed Luhn check)", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card number with sequential digits")
        void shouldRejectCardNumberWithSequentialDigits() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("1234567890123456"); // Fails Luhn

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid card number (failed Luhn check)", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate 15-digit card with Luhn")
        void shouldValidate15DigitCardWithLuhn() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("374245455400126"); // Valid 15-digit Amex with Luhn

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject valid length but invalid Luhn")
        void shouldRejectValidLengthButInvalidLuhn() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532015112830367"); // 16 digits but fails Luhn (last digit changed)

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid card number (failed Luhn check)", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate known test cards with Luhn algorithm")
        void shouldValidateKnownTestCardsWithLuhnAlgorithm() {
            String[] validTestCards = {
                "4532015112830366", // Visa
                "5425233430109903", // Mastercard
                "374245455400126",  // Amex
                "6011000991300009"  // Discover
            };

            for (String cardNumber : validTestCards) {
                CheckoutRequest request = createValidRequest();
                request.setCardNumber(cardNumber);
                assertDoesNotThrow(() -> paymentService.validateCardDetails(request),
                    "Card " + cardNumber + " should pass Luhn validation");
            }
        }
    }

    @Nested
    @DisplayName("Expiry Date Validation Tests")
    class ExpiryDateValidationTests {

        @Test
        @DisplayName("Should accept valid future expiry date")
        void shouldAcceptValidFutureExpiryDate() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("12/30"); // December 2030

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept current month and year")
        void shouldAcceptCurrentMonthAndYear() {
            CheckoutRequest request = createValidRequest();
            // Get current month/year dynamically
            java.time.YearMonth now = java.time.YearMonth.now();
            String currentDate = String.format("%02d/%02d", now.getMonthValue(), now.getYear() % 100);
            request.setExpiryDate(currentDate);

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept far future expiry date")
        void shouldAcceptFarFutureExpiryDate() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("12/35"); // 10+ years in future

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject null expiry date")
        void shouldRejectNullExpiryDate() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate(null);

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Expiry date is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject empty expiry date")
        void shouldRejectEmptyExpiryDate() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Expiry date is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject expired card")
        void shouldRejectExpiredCard() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("01/20"); // January 2020 - expired

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should reject invalid expiry date format with dashes")
        void shouldRejectInvalidExpiryDateFormatWithDashes() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("12-30");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should reject invalid expiry date format without separator")
        void shouldRejectInvalidExpiryDateFormatWithoutSeparator() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("1230");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should reject invalid month greater than 12")
        void shouldRejectInvalidMonthGreaterThan12() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("13/30");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should reject invalid month equal to 00")
        void shouldRejectInvalidMonthEqualTo00() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("00/30");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should reject expiry date with letters")
        void shouldRejectExpiryDateWithLetters() {
            CheckoutRequest request = createValidRequest();
            request.setExpiryDate("AB/30");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Invalid or expired card. Expiry date must be in MM/YY format and not expired",
                exception.getMessage());
        }
    }

    @Nested
    @DisplayName("CVV Validation Tests")
    class CVVValidationTests {

        @Test
        @DisplayName("Should accept valid 3-digit CVV")
        void shouldAcceptValid3DigitCVV() {
            CheckoutRequest request = createValidRequest();
            request.setCvv("123");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept valid 4-digit CVV for Amex")
        void shouldAcceptValid4DigitCVVForAmex() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("374245455400126"); // Amex
            request.setCvv("1234");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject null CVV")
        void shouldRejectNullCVV() {
            CheckoutRequest request = createValidRequest();
            request.setCvv(null);

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("CVV is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject empty CVV")
        void shouldRejectEmptyCVV() {
            CheckoutRequest request = createValidRequest();
            request.setCvv("");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("CVV is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject CVV shorter than 3 digits")
        void shouldRejectCVVShorterThan3Digits() {
            CheckoutRequest request = createValidRequest();
            request.setCvv("12");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("CVV must be 3-4 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject CVV longer than 4 digits")
        void shouldRejectCVVLongerThan4Digits() {
            CheckoutRequest request = createValidRequest();
            request.setCvv("12345");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("CVV must be 3-4 digits", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject CVV with non-numeric characters")
        void shouldRejectCVVWithNonNumericCharacters() {
            CheckoutRequest request = createValidRequest();
            request.setCvv("12A");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("CVV must be 3-4 digits", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Card Holder Name Validation Tests")
    class CardHolderNameValidationTests {

        @Test
        @DisplayName("Should accept valid card holder name")
        void shouldAcceptValidCardHolderName() {
            CheckoutRequest request = createValidRequest();
            request.setCardHolderName("John Doe");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should accept card holder name with multiple words")
        void shouldAcceptCardHolderNameWithMultipleWords() {
            CheckoutRequest request = createValidRequest();
            request.setCardHolderName("John Michael Doe");

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should reject null card holder name")
        void shouldRejectNullCardHolderName() {
            CheckoutRequest request = createValidRequest();
            request.setCardHolderName(null);

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card holder name is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject empty card holder name")
        void shouldRejectEmptyCardHolderName() {
            CheckoutRequest request = createValidRequest();
            request.setCardHolderName("");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card holder name is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject card holder name with only whitespace")
        void shouldRejectCardHolderNameWithOnlyWhitespace() {
            CheckoutRequest request = createValidRequest();
            request.setCardHolderName("   ");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.validateCardDetails(request)
            );
            assertEquals("Card holder name is required", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Complete Validation Flow Tests")
    class CompleteValidationFlowTests {

        @Test
        @DisplayName("Should pass validation with all valid details")
        void shouldPassValidationWithAllValidDetails() {
            CheckoutRequest request = new CheckoutRequest(
                "4532015112830366",
                "John Doe",
                "12/30",
                "123"
            );

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should validate complete request with spaces in card number")
        void shouldValidateCompleteRequestWithSpacesInCardNumber() {
            CheckoutRequest request = new CheckoutRequest(
                "4532 0151 1283 0366",
                "Jane Smith",
                "06/28",
                "456"
            );

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }

        @Test
        @DisplayName("Should validate Amex card with 4-digit CVV")
        void shouldValidateAmexCardWith4DigitCVV() {
            CheckoutRequest request = new CheckoutRequest(
                "374245455400126",
                "Alice Johnson",
                "03/27",
                "1234"
            );

            assertDoesNotThrow(() -> paymentService.validateCardDetails(request));
        }
    }

    @Nested
    @DisplayName("Fake Payment Processing Tests")
    class FakePaymentProcessingTests {

        @Test
        @DisplayName("Should process payment successfully with valid details")
        void shouldProcessPaymentSuccessfullyWithValidDetails() {
            CheckoutRequest request = createValidRequest();
            BigDecimal amount = new BigDecimal("100.00");

            boolean result = paymentService.processFakePayment(amount, request);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should process payment with large amount")
        void shouldProcessPaymentWithLargeAmount() {
            CheckoutRequest request = createValidRequest();
            BigDecimal largeAmount = new BigDecimal("9999.99");

            boolean result = paymentService.processFakePayment(largeAmount, request);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should process payment with small amount")
        void shouldProcessPaymentWithSmallAmount() {
            CheckoutRequest request = createValidRequest();
            BigDecimal smallAmount = new BigDecimal("0.01");

            boolean result = paymentService.processFakePayment(smallAmount, request);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should process payment with card number containing spaces")
        void shouldProcessPaymentWithCardNumberContainingSpaces() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("4532 0151 1283 0366");
            BigDecimal amount = new BigDecimal("50.00");

            boolean result = paymentService.processFakePayment(amount, request);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should throw exception for special decline card number")
        void shouldThrowExceptionForSpecialDeclineCardNumber() {
            CheckoutRequest request = createValidRequest();
            request.setCardNumber("0000000000000000");
            BigDecimal amount = new BigDecimal("100.00");

            PaymentValidationException exception = assertThrows(
                PaymentValidationException.class,
                () -> paymentService.processFakePayment(amount, request)
            );
            assertEquals("Payment declined by bank", exception.getMessage());
        }

        @Test
        @DisplayName("Should return true for all non-decline card numbers")
        void shouldReturnTrueForAllNonDeclineCardNumbers() {
            String[] validCards = {
                "4532015112830366",
                "5425233430109903",
                "374245455400126",
                "6011000991300009"
            };

            BigDecimal amount = new BigDecimal("75.50");

            for (String cardNumber : validCards) {
                CheckoutRequest request = createValidRequest();
                request.setCardNumber(cardNumber);
                boolean result = paymentService.processFakePayment(amount, request);
                assertTrue(result, "Payment should succeed for card: " + cardNumber);
            }
        }
    }

    // Helper method to create a valid checkout request
    private CheckoutRequest createValidRequest() {
        return new CheckoutRequest(
            "4532015112830366", // Valid Visa test card
            "Test User",
            "12/30",
            "123"
        );
    }
}
