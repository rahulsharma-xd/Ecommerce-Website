package com.ecommerce.app.controller;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.dto.OrderItemDTO;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.entity.OrderStatus;
import com.ecommerce.app.exception.InsufficientStockException;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.OrderNotFoundException;
import com.ecommerce.app.exception.PaymentValidationException;
import com.ecommerce.app.security.JwtAuthenticationFilter;
import com.ecommerce.app.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController.
 * Tests REST API endpoints with Spring Security using MockMvc.
 */
@WebMvcTest(value = OrderController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private CheckoutRequest validCheckoutRequest;
    private OrderResponse orderResponse1;
    private OrderResponse orderResponse2;
    private OrderItemDTO orderItem1;
    private OrderItemDTO orderItem2;

    @BeforeEach
    void setUp() {
        // Setup valid checkout request
        validCheckoutRequest = new CheckoutRequest(
            "4532015112830366",
            "Test User",
            "12/30",
            "123"
        );

        // Setup order items
        orderItem1 = new OrderItemDTO(
            1L,
            101L,
            "Laptop",
            "laptop.jpg",
            2,
            new BigDecimal("999.99"),
            new BigDecimal("1999.98")
        );

        orderItem2 = new OrderItemDTO(
            2L,
            102L,
            "Mouse",
            "mouse.jpg",
            1,
            new BigDecimal("49.99"),
            new BigDecimal("49.99")
        );

        // Setup order responses
        orderResponse1 = new OrderResponse(
            1L,
            "ORD-20250128-00001",
            OrderStatus.PENDING,
            new BigDecimal("2049.97"),
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            Arrays.asList(orderItem1, orderItem2)
        );

        orderResponse2 = new OrderResponse(
            2L,
            "ORD-20250128-00002",
            OrderStatus.SHIPPED,
            new BigDecimal("99.99"),
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now().minusDays(2),
            Collections.singletonList(orderItem2)
        );
    }

    @Nested
    @DisplayName("POST /api/orders/checkout - Checkout")
    class CheckoutTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should successfully checkout with valid cart and payment")
        void shouldSuccessfullyCheckout() throws Exception {
            // Given
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(2049.97))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[1].productName").value("Mouse"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to checkout")
        void shouldAllowAdminToCheckout() throws Exception {
            // Given
            when(orderService.checkout(eq("admin@example.com"), any(CheckoutRequest.class)))
                .thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when cart is empty")
        void shouldReturn400WhenCartIsEmpty() throws Exception {
            // Given
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenThrow(new InvalidInputException("Cart is empty. Add items before checkout."));

            // When/Then - Global exception handler maps InvalidInputException to 400 BAD REQUEST
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 409 when stock is insufficient")
        void shouldReturn409WhenStockIsInsufficient() throws Exception {
            // Given
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenThrow(new InsufficientStockException(
                    "Insufficient stock for product 'Laptop'. Available: 1, Requested: 2"));

            // When/Then - GlobalExceptionHandler returns 409 CONFLICT for InsufficientStockException
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Insufficient stock for product 'Laptop'. Available: 1, Requested: 2"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when card validation fails")
        void shouldReturn400WhenCardValidationFails() throws Exception {
            // Given
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenThrow(new PaymentValidationException("Invalid card number"));

            // When/Then - GlobalExceptionHandler returns 400 BAD REQUEST for PaymentValidationException
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid card number"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should successfully process checkout even with null card fields")
        void shouldProcessCheckoutEvenWithNullCardFields() throws Exception {
            // Given - Service layer handles validation, controller just passes through
            CheckoutRequest invalidRequest = new CheckoutRequest(null, "Test User", "12/30", "123");
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(orderResponse1);

            // When/Then - Controller doesn't validate, so request goes through
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should successfully checkout with single item")
        void shouldSuccessfullyCheckoutWithSingleItem() throws Exception {
            // Given
            OrderResponse singleItemOrder = new OrderResponse(
                1L,
                "ORD-20250128-00001",
                OrderStatus.PENDING,
                new BigDecimal("49.99"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Collections.singletonList(orderItem2)
            );
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(singleItemOrder);

            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(49.99))
                .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should handle large order amounts")
        void shouldHandleLargeOrderAmounts() throws Exception {
            // Given
            OrderResponse largeOrder = new OrderResponse(
                1L,
                "ORD-20250128-00001",
                OrderStatus.PENDING,
                new BigDecimal("99999.99"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(largeOrder);

            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(99999.99));
        }
    }

    @Nested
    @DisplayName("GET /api/orders - Get User Orders")
    class GetUserOrdersTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return user's order history")
        void shouldReturnUserOrderHistory() throws Exception {
            // Given
            List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2);
            when(orderService.getUserOrders("test@example.com")).thenReturn(orders);

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].totalAmount").value(2049.97))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].orderNumber").value("ORD-20250128-00002"))
                .andExpect(jsonPath("$[1].status").value("SHIPPED"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to view their orders")
        void shouldAllowAdminToViewOrders() throws Exception {
            // Given
            when(orderService.getUserOrders("admin@example.com"))
                .thenReturn(Collections.singletonList(orderResponse1));

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return empty list when user has no orders")
        void shouldReturnEmptyListWhenNoOrders() throws Exception {
            // Given
            when(orderService.getUserOrders("test@example.com"))
                .thenReturn(Collections.emptyList());

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return orders sorted by most recent first")
        void shouldReturnOrdersSortedByMostRecentFirst() throws Exception {
            // Given - orderResponse1 is more recent than orderResponse2
            List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2);
            when(orderService.getUserOrders("test@example.com")).thenReturn(orders);

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return orders with complete item details")
        void shouldReturnOrdersWithCompleteItemDetails() throws Exception {
            // Given
            when(orderService.getUserOrders("test@example.com"))
                .thenReturn(Collections.singletonList(orderResponse1));

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].productId").value(101L))
                .andExpect(jsonPath("$[0].items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[0].items[0].productImageUrl").value("laptop.jpg"))
                .andExpect(jsonPath("$[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$[0].items[0].priceAtPurchase").value(999.99))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(1999.98));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id} - Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return order details when user owns the order")
        void shouldReturnOrderDetailsWhenUserOwnsOrder() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 1L))
                .thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(2049.97))
                .andExpect(jsonPath("$.items", hasSize(2)));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to view their order")
        void shouldAllowAdminToViewTheirOrder() throws Exception {
            // Given
            when(orderService.getOrderById("admin@example.com", 1L))
                .thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 999L))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

            // When/Then - GlobalExceptionHandler returns 404 NOT FOUND for OrderNotFoundException
            mockMvc.perform(get("/api/orders/999")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 404 when order doesn't belong to user")
        void shouldReturn404WhenOrderDoesntBelongToUser() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 1L))
                .thenThrow(new OrderNotFoundException("Order does not belong to you"));

            // When/Then - GlobalExceptionHandler returns 404 NOT FOUND for OrderNotFoundException
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order does not belong to you"));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return order with all item details")
        void shouldReturnOrderWithAllItemDetails() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 1L))
                .thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].productId").value(101L))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].productImageUrl").value("laptop.jpg"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].priceAtPurchase").value(999.99))
                .andExpect(jsonPath("$.items[0].subtotal").value(1999.98))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.items[1].productName").value("Mouse"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return order with correct status")
        void shouldReturnOrderWithCorrectStatus() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 2L))
                .thenReturn(orderResponse2);

            // When/Then
            mockMvc.perform(get("/api/orders/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should handle different order IDs correctly")
        void shouldHandleDifferentOrderIdsCorrectly() throws Exception {
            // Given
            when(orderService.getOrderById("test@example.com", 1L))
                .thenReturn(orderResponse1);
            when(orderService.getOrderById("test@example.com", 2L))
                .thenReturn(orderResponse2);

            // When/Then - First order
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"));

            // When/Then - Second order
            mockMvc.perform(get("/api/orders/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00002"));
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for checkout")
        void shouldRequireAuthenticationForCheckout() throws Exception {
            // When/Then - Returns 401 UNAUTHORIZED when not authenticated
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should require authentication for get user orders")
        void shouldRequireAuthenticationForGetUserOrders() throws Exception {
            // When/Then - Returns 401 UNAUTHORIZED when not authenticated
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should require authentication for get order by ID")
        void shouldRequireAuthenticationForGetOrderById() throws Exception {
            // When/Then - Returns 401 UNAUTHORIZED when not authenticated
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should allow USER role to access all endpoints")
        void shouldAllowUserRoleToAccessAllEndpoints() throws Exception {
            // Given
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(orderResponse1);
            when(orderService.getUserOrders("test@example.com"))
                .thenReturn(Collections.singletonList(orderResponse1));
            when(orderService.getOrderById("test@example.com", 1L))
                .thenReturn(orderResponse1);

            // When/Then - Checkout
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated());

            // When/Then - Get orders
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // When/Then - Get order by ID
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN role to access all endpoints")
        void shouldAllowAdminRoleToAccessAllEndpoints() throws Exception {
            // Given
            when(orderService.checkout(eq("admin@example.com"), any(CheckoutRequest.class)))
                .thenReturn(orderResponse1);
            when(orderService.getUserOrders("admin@example.com"))
                .thenReturn(Collections.singletonList(orderResponse1));
            when(orderService.getOrderById("admin@example.com", 1L))
                .thenReturn(orderResponse1);

            // When/Then - Checkout
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated());

            // When/Then - Get orders
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // When/Then - Get order by ID
            mockMvc.perform(get("/api/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should handle order with very large ID")
        void shouldHandleOrderWithVeryLargeId() throws Exception {
            // Given
            Long largeId = 999999999L;
            OrderResponse largeIdOrder = new OrderResponse(
                largeId,
                "ORD-20250128-00001",
                OrderStatus.PENDING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Collections.singletonList(orderItem1)
            );
            when(orderService.getOrderById("test@example.com", largeId))
                .thenReturn(largeIdOrder);

            // When/Then
            mockMvc.perform(get("/api/orders/" + largeId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(largeId));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should handle order with many items")
        void shouldHandleOrderWithManyItems() throws Exception {
            // Given
            List<OrderItemDTO> manyItems = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                manyItems.add(new OrderItemDTO(
                    (long) i,
                    (long) i,
                    "Product " + i,
                    "image" + i + ".jpg",
                    1,
                    new BigDecimal("10.00"),
                    new BigDecimal("10.00")
                ));
            }
            OrderResponse manyItemsOrder = new OrderResponse(
                1L,
                "ORD-20250128-00001",
                OrderStatus.PENDING,
                new BigDecimal("500.00"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                manyItems
            );
            when(orderService.checkout(eq("test@example.com"), any(CheckoutRequest.class)))
                .thenReturn(manyItemsOrder);

            // When/Then
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCheckoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(50)));
        }

        @Test
        @WithMockUser(username = "user.with+special@example.com", roles = {"USER"})
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() throws Exception {
            // Given
            when(orderService.getUserOrders("user.with+special@example.com"))
                .thenReturn(Collections.singletonList(orderResponse1));

            // When/Then
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}
