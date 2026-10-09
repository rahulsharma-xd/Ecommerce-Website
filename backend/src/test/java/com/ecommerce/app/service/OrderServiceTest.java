package com.ecommerce.app.service;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.entity.*;
import com.ecommerce.app.exception.InsufficientStockException;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.OrderNotFoundException;
import com.ecommerce.app.exception.PaymentValidationException;
import com.ecommerce.app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for OrderService.
 * Tests checkout workflow, order retrieval, and admin operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct1;
    private Product testProduct2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private CheckoutRequest checkoutRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        // Setup test products
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Test Product 1");
        testProduct1.setPrice(new BigDecimal("29.99"));
        testProduct1.setStockQuantity(100);
        testProduct1.setActive(true);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(new BigDecimal("49.99"));
        testProduct2.setStockQuantity(50);
        testProduct2.setActive(true);

        // Setup cart items
        cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setProduct(testProduct1);
        cartItem1.setQuantity(2);
        cartItem1.setPriceAtAddition(new BigDecimal("29.99"));

        cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setProduct(testProduct2);
        cartItem2.setQuantity(1);
        cartItem2.setPriceAtAddition(new BigDecimal("49.99"));

        // Setup test cart
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>(Arrays.asList(cartItem1, cartItem2)));
        cartItem1.setCart(testCart);
        cartItem2.setCart(testCart);

        // Setup checkout request
        checkoutRequest = new CheckoutRequest(
            "4532015112830366",
            "Test User",
            "12/30",
            "123"
        );

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber("ORD-20250128-00001");
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("109.97"));
        testOrder.setOrderedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        testOrder.setOrderItems(new ArrayList<>());
    }

    @Nested
    @DisplayName("Checkout Tests")
    class CheckoutTests {

        @Test
        @DisplayName("Should successfully checkout with valid cart and payment")
        void shouldSuccessfullyCheckoutWithValidCartAndPayment() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("109.97"));
            assertThat(response.getItems()).hasSize(2);

            // Verify all steps were executed
            verify(paymentService).validateCardDetails(checkoutRequest);
            verify(userRepository).findByEmail("test@example.com");
            verify(cartRepository).findByUserId(1L);
            verify(paymentService).processFakePayment(any(BigDecimal.class), eq(checkoutRequest));
            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository, times(2)).save(any(OrderItem.class));
            verify(productRepository, times(2)).save(any(Product.class));
            verify(cartItemRepository).deleteAll(anyList());
            verify(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
        }

        @Test
        @DisplayName("Should deduct stock correctly during checkout")
        void shouldDeductStockCorrectlyDuringCheckout() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            int initialStock1 = testProduct1.getStockQuantity();
            int initialStock2 = testProduct2.getStockQuantity();

            // Act
            orderService.checkout("test@example.com", checkoutRequest);

            // Assert - verify stock was deducted
            assertThat(testProduct1.getStockQuantity()).isEqualTo(initialStock1 - 2);
            assertThat(testProduct2.getStockQuantity()).isEqualTo(initialStock2 - 1);
            verify(productRepository, times(2)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should clear cart after successful checkout")
        void shouldClearCartAfterSuccessfulCheckout() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            verify(cartItemRepository).deleteAll(testCart.getCartItems());
            verify(cartRepository).save(testCart);
        }

        @Test
        @DisplayName("Should send confirmation email after checkout")
        void shouldSendConfirmationEmailAfterCheckout() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(emailService).sendOrderConfirmation(userCaptor.capture(), orderCaptor.capture());

            assertThat(userCaptor.getValue().getEmail()).isEqualTo("test@example.com");
            assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.checkout("nonexistent@example.com", checkoutRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowExceptionWhenCartNotFound() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.checkout("test@example.com", checkoutRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Cart not found");
        }

        @Test
        @DisplayName("Should throw exception when cart is empty")
        void shouldThrowExceptionWhenCartIsEmpty() {
            // Arrange
            testCart.setCartItems(new ArrayList<>());
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            // Act & Assert
            assertThatThrownBy(() -> orderService.checkout("test@example.com", checkoutRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Cart is empty");
        }

        @Test
        @DisplayName("Should throw exception when stock is insufficient")
        void shouldThrowExceptionWhenStockIsInsufficient() {
            // Arrange
            testProduct1.setStockQuantity(1); // Less than requested quantity of 2
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

            // Act & Assert
            assertThatThrownBy(() -> orderService.checkout("test@example.com", checkoutRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("Test Product 1")
                .hasMessageContaining("Available: 1")
                .hasMessageContaining("Requested: 2");
        }

        @Test
        @DisplayName("Should throw exception when payment validation fails")
        void shouldThrowExceptionWhenPaymentValidationFails() {
            // Arrange
            doThrow(new PaymentValidationException("Invalid card number"))
                .when(paymentService).validateCardDetails(any(CheckoutRequest.class));

            // Act & Assert
            assertThatThrownBy(() -> orderService.checkout("test@example.com", checkoutRequest))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("Invalid card number");
        }

        @Test
        @DisplayName("Should calculate correct total amount")
        void shouldCalculateCorrectTotalAmount() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Expected: (29.99 * 2) + (49.99 * 1) = 59.98 + 49.99 = 109.97

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("109.97"));
        }

        @Test
        @DisplayName("Should generate valid order number")
        void shouldGenerateValidOrderNumber() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.count()).thenReturn(5L);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response.getOrderNumber())
                .startsWith("ORD-")
                .matches("ORD-\\d{8}-\\d{5}");
        }
    }

    @Nested
    @DisplayName("Get User Orders Tests")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Should return user's orders sorted by date")
        void shouldReturnUserOrdersSortedByDate() {
            // Arrange
            Order order1 = createOrder(1L, "ORD-20250128-00001", LocalDateTime.now().minusDays(2));
            Order order2 = createOrder(2L, "ORD-20250128-00002", LocalDateTime.now().minusDays(1));

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(orderRepository.findByUserIdOrderByOrderedAtDesc(1L))
                .thenReturn(Arrays.asList(order2, order1));

            // Act
            List<OrderResponse> responses = orderService.getUserOrders("test@example.com");

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getOrderNumber()).isEqualTo("ORD-20250128-00002");
            assertThat(responses.get(1).getOrderNumber()).isEqualTo("ORD-20250128-00001");
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void shouldReturnEmptyListWhenUserHasNoOrders() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(orderRepository.findByUserIdOrderByOrderedAtDesc(1L))
                .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponse> responses = orderService.getUserOrders("test@example.com");

            // Assert
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getUserOrders("nonexistent@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Get Order By ID Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when user owns it")
        void shouldReturnOrderWhenUserOwnsIt() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // Act
            OrderResponse response = orderService.getOrderById("test@example.com", 1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getOrderNumber()).isEqualTo("ORD-20250128-00001");
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById("test@example.com", 999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own the order")
        void shouldThrowExceptionWhenUserDoesntOwnTheOrder() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(2L);
            anotherUser.setEmail("another@example.com");

            when(userRepository.findByEmail("another@example.com")).thenReturn(Optional.of(anotherUser));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById("another@example.com", 1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("does not belong to you");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById("nonexistent@example.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {

        @Test
        @DisplayName("Should return all orders for admin")
        void shouldReturnAllOrdersForAdmin() {
            // Arrange
            Order order1 = createOrder(1L, "ORD-20250128-00001", LocalDateTime.now().minusDays(2));
            Order order2 = createOrder(2L, "ORD-20250128-00002", LocalDateTime.now().minusDays(1));

            when(orderRepository.findAllByOrderByOrderedAtDesc())
                .thenReturn(Arrays.asList(order2, order1));

            // Act
            List<OrderResponse> responses = orderService.getAllOrders();

            // Assert
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("Should get order by ID without ownership check")
        void shouldGetOrderByIdWithoutOwnershipCheck() {
            // Arrange
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // Act
            OrderResponse response = orderService.getOrderByIdAdmin(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when admin gets non-existent order")
        void shouldThrowExceptionWhenAdminGetsNonExistentOrder() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderByIdAdmin(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() {
            // Arrange
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

            // Assert
            assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent order status")
        void shouldThrowExceptionWhenUpdatingNonExistentOrderStatus() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.SHIPPED))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("Should update order status to all possible statuses")
        void shouldUpdateOrderStatusToAllPossibleStatuses() {
            // Arrange
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Test all statuses
            OrderStatus[] statuses = {
                OrderStatus.PENDING,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED
            };

            // Act & Assert
            for (OrderStatus status : statuses) {
                OrderResponse response = orderService.updateOrderStatus(1L, status);
                assertThat(response.getStatus()).isEqualTo(status);
            }
        }
    }

    @Nested
    @DisplayName("Order Number Generation Tests")
    class OrderNumberGenerationTests {

        @Test
        @DisplayName("Should generate order numbers with correct format")
        void shouldGenerateOrderNumbersWithCorrectFormat() {
            // Arrange - need to create fresh cart items for each call since they get cleared
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(orderRepository.count()).thenReturn(0L, 1L, 2L);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Reset cart items before each checkout since they get cleared
            when(cartRepository.findByUserId(1L)).thenAnswer(invocation -> {
                testCart.setCartItems(new ArrayList<>(Arrays.asList(cartItem1, cartItem2)));
                return Optional.of(testCart);
            });

            // Act
            OrderResponse response1 = orderService.checkout("test@example.com", checkoutRequest);
            OrderResponse response2 = orderService.checkout("test@example.com", checkoutRequest);
            OrderResponse response3 = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response1.getOrderNumber()).matches("ORD-\\d{8}-00001");
            assertThat(response2.getOrderNumber()).matches("ORD-\\d{8}-00002");
            assertThat(response3.getOrderNumber()).matches("ORD-\\d{8}-00003");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle checkout with single item in cart")
        void shouldHandleCheckoutWithSingleItemInCart() {
            // Arrange
            testCart.setCartItems(new ArrayList<>(Arrays.asList(cartItem1)));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));
        }

        @Test
        @DisplayName("Should handle checkout when stock exactly matches requested quantity")
        void shouldHandleCheckoutWhenStockExactlyMatchesRequestedQuantity() {
            // Arrange
            testProduct1.setStockQuantity(2); // Exactly what's requested
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(testProduct1.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle large quantity orders")
        void shouldHandleLargeQuantityOrders() {
            // Arrange
            cartItem1.setQuantity(100);
            testProduct1.setStockQuantity(100);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(testProduct1.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle order with very high total amount")
        void shouldHandleOrderWithVeryHighTotalAmount() {
            // Arrange
            testProduct1.setPrice(new BigDecimal("9999.99"));
            cartItem1.setQuantity(10);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(paymentService).validateCardDetails(any(CheckoutRequest.class));
            when(paymentService.processFakePayment(any(BigDecimal.class), any(CheckoutRequest.class))).thenReturn(true);
            doNothing().when(emailService).sendOrderConfirmation(any(User.class), any(Order.class));
            doNothing().when(cartItemRepository).deleteAll(anyList());

            // Act
            OrderResponse response = orderService.checkout("test@example.com", checkoutRequest);

            // Assert
            assertThat(response.getTotalAmount()).isGreaterThan(new BigDecimal("99999"));
        }
    }

    // Helper method to create test orders
    private Order createOrder(Long id, String orderNumber, LocalDateTime orderedAt) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setOrderedAt(orderedAt);
        order.setUpdatedAt(orderedAt);
        order.setOrderItems(new ArrayList<>());
        return order;
    }
}
