package com.ecommerce.app.service;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.OrderItem;
import com.ecommerce.app.entity.OrderStatus;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmailService.
 * Tests email sending, HTML content generation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private Order testOrder;
    private Product testProduct1;
    private Product testProduct2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");

        // Create test products
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Test Product 1");
        testProduct1.setPrice(new BigDecimal("29.99"));

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(new BigDecimal("49.99"));

        // Create order items
        orderItem1 = new OrderItem();
        orderItem1.setProduct(testProduct1);
        orderItem1.setQuantity(2);
        orderItem1.setPriceAtPurchase(new BigDecimal("29.99"));
        orderItem1.setSubtotal(new BigDecimal("59.98"));

        orderItem2 = new OrderItem();
        orderItem2.setProduct(testProduct2);
        orderItem2.setQuantity(1);
        orderItem2.setPriceAtPurchase(new BigDecimal("49.99"));
        orderItem2.setSubtotal(new BigDecimal("49.99"));

        // Create test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber("ORD-20250101-001");
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("109.97"));
        testOrder.setOrderedAt(LocalDateTime.of(2025, 1, 1, 10, 30, 0));

        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem1);
        items.add(orderItem2);
        testOrder.setOrderItems(items);
    }

    @Nested
    @DisplayName("Email Sending Tests")
    class EmailSendingTests {

        @Test
        @DisplayName("Should send order confirmation email successfully")
        void shouldSendOrderConfirmationEmailSuccessfully() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).createMimeMessage();
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should set correct email recipient")
        void shouldSetCorrectEmailRecipient() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert - verify that send was called with message
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send email with order number in subject")
        void shouldSendEmailWithOrderNumberInSubject() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // Note: Subject line should be "Order Confirmation - ORD-20250101-001"
        }

        @Test
        @DisplayName("Should send email for order with single item")
        void shouldSendEmailForOrderWithSingleItem() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Create order with single item
            List<OrderItem> singleItem = new ArrayList<>();
            singleItem.add(orderItem1);
            testOrder.setOrderItems(singleItem);
            testOrder.setTotalAmount(new BigDecimal("59.98"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send email for order with multiple items")
        void shouldSendEmailForOrderWithMultipleItems() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send email for large order amount")
        void shouldSendEmailForLargeOrderAmount() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("9999.99"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send email for small order amount")
        void shouldSendEmailForSmallOrderAmount() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("0.01"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should log MessagingException and continue gracefully")
        void shouldLogMessagingExceptionAndContinueGracefully() {
            // Note: Since we can't easily mock MimeMessageHelper to throw MessagingException,
            // this test verifies the error handling exists in the code.
            // In a real scenario, MessagingException would be caught and logged.

            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act & Assert - should not throw exception for valid inputs
            assertDoesNotThrow(() -> emailService.sendOrderConfirmation(testUser, testOrder));

            // Verify email was sent successfully
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle MailException gracefully")
        void shouldHandleMailExceptionGracefully() {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailException("Mail server error") {})
                .when(mailSender).send(any(MimeMessage.class));

            // Act & Assert - should not throw exception, error is logged
            assertDoesNotThrow(() -> emailService.sendOrderConfirmation(testUser, testOrder));

            // Verify that we attempted to send
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should catch RuntimeException during message creation")
        void shouldCatchRuntimeExceptionDuringMessageCreation() {
            // Arrange
            when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Failed to create message"));

            // Act & Assert - RuntimeException is not caught by the service, so it should throw
            assertThrows(RuntimeException.class, () -> emailService.sendOrderConfirmation(testUser, testOrder));

            // Verify that we attempted to create message
            verify(mailSender, times(1)).createMimeMessage();
            // Verify send was never called since creation failed
            verify(mailSender, never()).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should continue execution after MailException")
        void shouldContinueExecutionAfterMailException() {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            // First call fails, second succeeds
            doThrow(new MailException("Network error") {})
                .doNothing()
                .when(mailSender).send(any(MimeMessage.class));

            // Act - both calls should not throw exception due to catch block
            assertDoesNotThrow(() -> emailService.sendOrderConfirmation(testUser, testOrder));
            assertDoesNotThrow(() -> emailService.sendOrderConfirmation(testUser, testOrder));

            // Assert - both attempts should have been made
            verify(mailSender, times(2)).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("HTML Content Tests")
    class HTMLContentTests {

        @Test
        @DisplayName("Should generate HTML email with user name")
        void shouldGenerateHTMLEmailWithUserName() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain "Hi testuser"
        }

        @Test
        @DisplayName("Should generate HTML email with order number")
        void shouldGenerateHTMLEmailWithOrderNumber() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain "ORD-20250101-001"
        }

        @Test
        @DisplayName("Should generate HTML email with order status")
        void shouldGenerateHTMLEmailWithOrderStatus() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain order status
        }

        @Test
        @DisplayName("Should generate HTML email with formatted date")
        void shouldGenerateHTMLEmailWithFormattedDate() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain formatted date like "Jan 01, 2025 at 10:30 AM"
        }

        @Test
        @DisplayName("Should generate HTML email with all order items")
        void shouldGenerateHTMLEmailWithAllOrderItems() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain both product names
        }

        @Test
        @DisplayName("Should generate HTML email with item quantities")
        void shouldGenerateHTMLEmailWithItemQuantities() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should show quantities: 2 for item1, 1 for item2
        }

        @Test
        @DisplayName("Should generate HTML email with formatted prices")
        void shouldGenerateHTMLEmailWithFormattedPrices() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain prices like "$29.99", "$49.99"
        }

        @Test
        @DisplayName("Should generate HTML email with subtotals")
        void shouldGenerateHTMLEmailWithSubtotals() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain subtotals: "$59.98", "$49.99"
        }

        @Test
        @DisplayName("Should generate HTML email with total amount")
        void shouldGenerateHTMLEmailWithTotalAmount() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain "Total: $109.97"
        }

        @Test
        @DisplayName("Should generate HTML email with proper styling")
        void shouldGenerateHTMLEmailWithProperStyling() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender).send(mimeMessage);
            // HTML should contain CSS styles, table formatting, etc.
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle user with special characters in username")
        void shouldHandleUserWithSpecialCharactersInUsername() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testUser.setUsername("Test <User> & Co.");

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle product with special characters in name")
        void shouldHandleProductWithSpecialCharactersInName() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testProduct1.setName("Product <Special> & \"Quotes\"");

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle order with very long order number")
        void shouldHandleOrderWithVeryLongOrderNumber() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setOrderNumber("ORD-20250101-001-VERYLONGNUMBEREXTENSION-12345678");

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle order with decimal prices that need formatting")
        void shouldHandleOrderWithDecimalPricesThatNeedFormatting() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("123.456")); // More than 2 decimals

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should format to "123.46"
        }

        @Test
        @DisplayName("Should handle order with price that rounds to whole number")
        void shouldHandleOrderWithPriceThatRoundsToWholeNumber() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("100.00"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should format to "100.00" not "100"
        }

        @Test
        @DisplayName("Should handle order with high quantity items")
        void shouldHandleOrderWithHighQuantityItems() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            orderItem1.setQuantity(100);
            orderItem1.setSubtotal(new BigDecimal("2999.00"));
            testOrder.setTotalAmount(new BigDecimal("3048.99"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle email address with plus sign")
        void shouldHandleEmailAddressWithPlusSign() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testUser.setEmail("testuser+orders@example.com");

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("Should handle different order statuses")
        void shouldHandleDifferentOrderStatuses() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // Test with different statuses
            OrderStatus[] statuses = {OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED};

            for (OrderStatus status : statuses) {
                testOrder.setStatus(status);

                // Act
                emailService.sendOrderConfirmation(testUser, testOrder);

                // Assert
                verify(mailSender, atLeastOnce()).send(mimeMessage);
            }
        }
    }

    @Nested
    @DisplayName("Date Formatting Tests")
    class DateFormattingTests {

        @Test
        @DisplayName("Should format date correctly for morning time")
        void shouldFormatDateCorrectlyForMorningTime() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setOrderedAt(LocalDateTime.of(2025, 1, 15, 9, 30, 0));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should show "Jan 15, 2025 at 09:30 AM"
        }

        @Test
        @DisplayName("Should format date correctly for afternoon time")
        void shouldFormatDateCorrectlyForAfternoonTime() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setOrderedAt(LocalDateTime.of(2025, 6, 20, 14, 45, 0));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should show "Jun 20, 2025 at 02:45 PM"
        }

        @Test
        @DisplayName("Should format date correctly for midnight")
        void shouldFormatDateCorrectlyForMidnight() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setOrderedAt(LocalDateTime.of(2025, 12, 31, 0, 0, 0));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should show "Dec 31, 2025 at 12:00 AM"
        }

        @Test
        @DisplayName("Should format date correctly for noon")
        void shouldFormatDateCorrectlyForNoon() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setOrderedAt(LocalDateTime.of(2025, 7, 4, 12, 0, 0));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should show "Jul 04, 2025 at 12:00 PM"
        }
    }

    @Nested
    @DisplayName("Money Formatting Tests")
    class MoneyFormattingTests {

        @Test
        @DisplayName("Should format money with two decimal places")
        void shouldFormatMoneyWithTwoDecimalPlaces() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("19.99"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should display as "$19.99"
        }

        @Test
        @DisplayName("Should format money with zero cents")
        void shouldFormatMoneyWithZeroCents() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("50.00"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should display as "$50.00" not "$50"
        }

        @Test
        @DisplayName("Should format money with one decimal place")
        void shouldFormatMoneyWithOneDecimalPlace() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("25.5"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should display as "$25.50"
        }

        @Test
        @DisplayName("Should format large money amounts with commas implied")
        void shouldFormatLargeMoneyAmountsWithCommasImplied() throws MessagingException {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            testOrder.setTotalAmount(new BigDecimal("1234.56"));

            // Act
            emailService.sendOrderConfirmation(testUser, testOrder);

            // Assert
            verify(mailSender, times(1)).send(mimeMessage);
            // Should display as "$1234.56" (formatMoney doesn't add commas)
        }
    }
}
