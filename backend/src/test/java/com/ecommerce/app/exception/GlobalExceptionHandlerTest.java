package com.ecommerce.app.exception;

import com.ecommerce.app.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 * Validates proper HTTP status codes and error response format for all exception types.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("InvalidInputException Handler Tests")
    class InvalidInputExceptionTests {

        @Test
        @DisplayName("Should return 400 BAD REQUEST for InvalidInputException")
        void shouldReturn400ForInvalidInputException() {
            // Given
            String errorMessage = "Cart is empty. Add items before checkout.";
            InvalidInputException exception = new InvalidInputException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidInput(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should preserve full error message")
        void shouldPreserveFullErrorMessage() {
            // Given
            String longMessage = "Invalid input: The product ID must be a positive integer greater than zero, " +
                "and the quantity must be between 1 and 100 units.";
            InvalidInputException exception = new InvalidInputException(longMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidInput(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo(longMessage);
        }
    }

    @Nested
    @DisplayName("UserAlreadyExistsException Handler Tests")
    class UserAlreadyExistsExceptionTests {

        @Test
        @DisplayName("Should return 409 CONFLICT for UserAlreadyExistsException")
        void shouldReturn409ForUserAlreadyExistsException() {
            // Given
            String errorMessage = "User with email test@example.com already exists";
            UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserAlreadyExists(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("InvalidCredentialsException Handler Tests")
    class InvalidCredentialsExceptionTests {

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED for InvalidCredentialsException")
        void shouldReturn401ForInvalidCredentialsException() {
            // Given
            String errorMessage = "Invalid email or password";
            InvalidCredentialsException exception = new InvalidCredentialsException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(401);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("AccessDeniedException Handler Tests")
    class AccessDeniedExceptionTests {

        @Test
        @DisplayName("Should return 403 FORBIDDEN for AccessDeniedException")
        void shouldReturn403ForAccessDeniedException() {
            // Given
            AccessDeniedException exception = new AccessDeniedException("Access is denied");

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(403);
            assertThat(response.getBody().getMessage())
                .isEqualTo("Access denied. You do not have the required permissions to access this resource.");
        }

        @Test
        @DisplayName("Should use custom message instead of exception message")
        void shouldUseCustomMessageForAccessDenied() {
            // Given
            AccessDeniedException exception = new AccessDeniedException("Original message");

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage())
                .isEqualTo("Access denied. You do not have the required permissions to access this resource.")
                .isNotEqualTo("Original message");
        }
    }

    @Nested
    @DisplayName("InsufficientStockException Handler Tests")
    class InsufficientStockExceptionTests {

        @Test
        @DisplayName("Should return 409 CONFLICT for InsufficientStockException")
        void shouldReturn409ForInsufficientStockException() {
            // Given
            String errorMessage = "Insufficient stock for product 'Laptop'. Available: 5, Requested: 10";
            InsufficientStockException exception = new InsufficientStockException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientStock(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should preserve stock quantity information in message")
        void shouldPreserveStockQuantityInformation() {
            // Given
            String errorMessage = "Insufficient stock for product 'Gaming Mouse'. Available: 0, Requested: 3";
            InsufficientStockException exception = new InsufficientStockException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientStock(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage())
                .contains("Available: 0")
                .contains("Requested: 3")
                .contains("Gaming Mouse");
        }
    }

    @Nested
    @DisplayName("PaymentValidationException Handler Tests")
    class PaymentValidationExceptionTests {

        @Test
        @DisplayName("Should return 400 BAD REQUEST for PaymentValidationException")
        void shouldReturn400ForPaymentValidationException() {
            // Given
            String errorMessage = "Invalid card number format";
            PaymentValidationException exception = new PaymentValidationException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handlePaymentValidation(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should handle expired card validation error")
        void shouldHandleExpiredCardError() {
            // Given
            String errorMessage = "Card has expired";
            PaymentValidationException exception = new PaymentValidationException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handlePaymentValidation(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Card has expired");
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("Should handle invalid CVV validation error")
        void shouldHandleInvalidCvvError() {
            // Given
            String errorMessage = "Invalid CVV format";
            PaymentValidationException exception = new PaymentValidationException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handlePaymentValidation(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid CVV format");
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("OrderNotFoundException Handler Tests")
    class OrderNotFoundExceptionTests {

        @Test
        @DisplayName("Should return 404 NOT FOUND for OrderNotFoundException")
        void shouldReturn404ForOrderNotFoundException() {
            // Given
            String errorMessage = "Order not found with id: 123";
            OrderNotFoundException exception = new OrderNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleOrderNotFound(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should handle order ownership validation error")
        void shouldHandleOrderOwnershipError() {
            // Given
            String errorMessage = "Order does not belong to you";
            OrderNotFoundException exception = new OrderNotFoundException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleOrderNotFound(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Order does not belong to you");
            assertThat(response.getBody().getStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("RuntimeException Handler Tests")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("Should return 500 INTERNAL SERVER ERROR for RuntimeException")
        void shouldReturn500ForRuntimeException() {
            // Given
            String errorMessage = "Unexpected error occurred";
            RuntimeException exception = new RuntimeException(errorMessage);

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should handle any unhandled runtime exception")
        void shouldHandleAnyUnhandledRuntimeException() {
            // Given
            RuntimeException exception = new NullPointerException("Null pointer encountered");

            // When
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getMessage()).isEqualTo("Null pointer encountered");
        }
    }

    @Nested
    @DisplayName("Error Response Format Tests")
    class ErrorResponseFormatTests {

        @Test
        @DisplayName("All handlers should return ErrorResponse with consistent format")
        void allHandlersShouldReturnConsistentFormat() {
            // Test InvalidInputException
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleInvalidInput(
                new InvalidInputException("Test message"));
            assertThat(response1.getBody()).isNotNull();
            assertThat(response1.getBody().getStatus()).isNotNull();
            assertThat(response1.getBody().getMessage()).isNotNull();

            // Test InsufficientStockException
            ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleInsufficientStock(
                new InsufficientStockException("Test message"));
            assertThat(response2.getBody()).isNotNull();
            assertThat(response2.getBody().getStatus()).isNotNull();
            assertThat(response2.getBody().getMessage()).isNotNull();

            // Test PaymentValidationException
            ResponseEntity<ErrorResponse> response3 = exceptionHandler.handlePaymentValidation(
                new PaymentValidationException("Test message"));
            assertThat(response3.getBody()).isNotNull();
            assertThat(response3.getBody().getStatus()).isNotNull();
            assertThat(response3.getBody().getMessage()).isNotNull();

            // Test OrderNotFoundException
            ResponseEntity<ErrorResponse> response4 = exceptionHandler.handleOrderNotFound(
                new OrderNotFoundException("Test message"));
            assertThat(response4.getBody()).isNotNull();
            assertThat(response4.getBody().getStatus()).isNotNull();
            assertThat(response4.getBody().getMessage()).isNotNull();
        }

        @Test
        @DisplayName("Status code in body should match HTTP status code")
        void statusCodeInBodyShouldMatchHttpStatusCode() {
            // Test InsufficientStockException (409)
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleInsufficientStock(
                new InsufficientStockException("Test"));
            assertThat(response1.getStatusCode().value()).isEqualTo(response1.getBody().getStatus());

            // Test PaymentValidationException (400)
            ResponseEntity<ErrorResponse> response2 = exceptionHandler.handlePaymentValidation(
                new PaymentValidationException("Test"));
            assertThat(response2.getStatusCode().value()).isEqualTo(response2.getBody().getStatus());

            // Test OrderNotFoundException (404)
            ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleOrderNotFound(
                new OrderNotFoundException("Test"));
            assertThat(response3.getStatusCode().value()).isEqualTo(response3.getBody().getStatus());
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Validation Tests")
    class HttpStatusCodeValidationTests {

        @Test
        @DisplayName("Should use correct HTTP status codes for client errors (4xx)")
        void shouldUseCorrectClientErrorStatusCodes() {
            // 400 BAD REQUEST
            assertThat(exceptionHandler.handleInvalidInput(
                new InvalidInputException("test")).getStatusCode().value()).isEqualTo(400);
            assertThat(exceptionHandler.handlePaymentValidation(
                new PaymentValidationException("test")).getStatusCode().value()).isEqualTo(400);

            // 401 UNAUTHORIZED
            assertThat(exceptionHandler.handleInvalidCredentials(
                new InvalidCredentialsException("test")).getStatusCode().value()).isEqualTo(401);

            // 403 FORBIDDEN
            assertThat(exceptionHandler.handleAccessDenied(
                new AccessDeniedException("test")).getStatusCode().value()).isEqualTo(403);

            // 404 NOT FOUND
            assertThat(exceptionHandler.handleOrderNotFound(
                new OrderNotFoundException("test")).getStatusCode().value()).isEqualTo(404);

            // 409 CONFLICT
            assertThat(exceptionHandler.handleUserAlreadyExists(
                new UserAlreadyExistsException("test")).getStatusCode().value()).isEqualTo(409);
            assertThat(exceptionHandler.handleInsufficientStock(
                new InsufficientStockException("test")).getStatusCode().value()).isEqualTo(409);
        }

        @Test
        @DisplayName("Should use correct HTTP status codes for server errors (5xx)")
        void shouldUseCorrectServerErrorStatusCodes() {
            // 500 INTERNAL SERVER ERROR
            assertThat(exceptionHandler.handleRuntimeException(
                new RuntimeException("test")).getStatusCode().value()).isEqualTo(500);
        }
    }
}
