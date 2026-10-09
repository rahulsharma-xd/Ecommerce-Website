package com.ecommerce.app.controller;

import com.ecommerce.app.dto.DeleteResponse;
import com.ecommerce.app.dto.InvalidateTokensResponse;
import com.ecommerce.app.dto.OrderItemDTO;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.dto.UpdateOrderStatusRequest;
import com.ecommerce.app.dto.UserResponse;
import com.ecommerce.app.entity.OrderStatus;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.OrderNotFoundException;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtAuthenticationFilter;
import com.ecommerce.app.security.JwtUtil;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.ProductService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController.
 * Tests ADMIN-only REST API endpoints with role-based access control.
 *
 * Note: @PreAuthorize role-based authorization (403 Forbidden for USER role)
 * requires full Spring Security context and is tested in integration tests.
 * These unit tests focus on functional behavior with proper ADMIN credentials.
 */
@WebMvcTest(value = AdminController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc
@DisplayName("AdminController Integration Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private User adminUser;
    private UserResponse userResponse;
    private ProductResponse productResponse;
    private ProductRequest productRequest;
    private OrderResponse orderResponse1;
    private OrderResponse orderResponse2;
    private OrderItemDTO orderItem1;
    private OrderItemDTO orderItem2;
    private UpdateOrderStatusRequest statusRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setActive(true);
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);

        // Setup admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setCreatedAt(now);
        adminUser.setUpdatedAt(now);

        // Setup user response
        userResponse = new UserResponse(
            1L, "testuser", "test@example.com", "USER", true, now, now
        );

        // Setup product response
        productResponse = new ProductResponse(
            1L, "Laptop", "High performance laptop",
            new BigDecimal("999.99"), 50, "Electronics",
            "Dell", "laptop.jpg", true, now, now
        );

        // Setup product request
        productRequest = new ProductRequest();
        productRequest.setName("Laptop");
        productRequest.setDescription("High performance laptop");
        productRequest.setPrice(new BigDecimal("999.99"));
        productRequest.setStockQuantity(50);
        productRequest.setCategory("Electronics");
        productRequest.setBrand("Dell");
        productRequest.setImageUrl("laptop.jpg");

        // Setup order items
        orderItem1 = new OrderItemDTO(
            1L, 101L, "Laptop", "laptop.jpg",
            2, new BigDecimal("999.99"), new BigDecimal("1999.98")
        );
        orderItem2 = new OrderItemDTO(
            2L, 102L, "Mouse", "mouse.jpg",
            1, new BigDecimal("49.99"), new BigDecimal("49.99")
        );

        // Setup order responses
        orderResponse1 = new OrderResponse(
            1L, "ORD-20250128-00001", OrderStatus.PENDING,
            new BigDecimal("2049.97"), now, now,
            Arrays.asList(orderItem1, orderItem2)
        );
        orderResponse2 = new OrderResponse(
            2L, "ORD-20250128-00002", OrderStatus.SHIPPED,
            new BigDecimal("999.99"), now, now,
            Arrays.asList(orderItem1)
        );

        // Setup status update request
        statusRequest = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);
    }

    @Nested
    @DisplayName("GET /api/admin/debug-auth - Debug Authentication")
    class DebugAuthTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return authentication details for authenticated user")
        void shouldReturnAuthenticationDetails() throws Exception {
            mockMvc.perform(get("/api/admin/debug-auth")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin@example.com"))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.authorities").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/admin-only - Admin Only Endpoint")
    class AdminOnlyTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN access")
        void shouldAllowAdminAccess() throws Exception {
            mockMvc.perform(get("/api/admin/admin-only")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        // Note: 403 Forbidden tests for USER role require full Security context
        // and are tested in integration tests (Phase 5)

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/admin-only")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users - Get All Users")
    class GetAllUsersTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return all users for ADMIN")
        void shouldReturnAllUsers() throws Exception {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, adminUser));

            // When/Then
            mockMvc.perform(get("/api/admin/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("admin"));
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).deleteById(1L);

            // When/Then
            mockMvc.perform(delete("/api/admin/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.deleted").value(true));

            verify(userRepository).deleteById(1L);
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 500 when user not found")
        void shouldReturn500WhenUserNotFound() throws Exception {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(delete("/api/admin/users/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("GET /api/admin/products - Get All Products")
    class GetAllProductsTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return all products including inactive")
        void shouldReturnAllProducts() throws Exception {
            // Given
            when(productService.getAllProducts()).thenReturn(Arrays.asList(productResponse));

            // When/Then
            mockMvc.perform(get("/api/admin/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("POST /api/admin/products - Create Product")
    class CreateProductTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() throws Exception {
            // Given
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

            // When/Then
            mockMvc.perform(post("/api/admin/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given
            when(productService.createProduct(any(ProductRequest.class)))
                .thenThrow(new InvalidInputException("Product name is required"));

            // When/Then
            mockMvc.perform(post("/api/admin/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest());
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("PUT /api/admin/products/{id} - Update Product")
    class UpdateProductTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            // Given
            when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(productResponse);

            // When/Then
            mockMvc.perform(put("/api/admin/products/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 500 when product not found")
        void shouldReturn500WhenProductNotFound() throws Exception {
            // Given
            when(productService.updateProduct(eq(999L), any(ProductRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

            // When/Then
            mockMvc.perform(put("/api/admin/products/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().is5xxServerError());
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("DELETE /api/admin/products/{id} - Delete Product")
    class DeleteProductTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should soft delete product successfully")
        void shouldSoftDeleteProductSuccessfully() throws Exception {
            // Given
            when(productService.getProductById(1L)).thenReturn(productResponse);
            doNothing().when(productService).deleteProduct(1L);

            // When/Then
            mockMvc.perform(delete("/api/admin/products/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.deleted").value(true));

            verify(productService).deleteProduct(1L);
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 500 when product not found")
        void shouldReturn500WhenProductNotFound() throws Exception {
            // Given
            when(productService.getProductById(999L))
                .thenThrow(new RuntimeException("Product not found"));

            // When/Then
            mockMvc.perform(delete("/api/admin/products/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("GET /api/admin/orders - Get All Orders")
    class GetAllOrdersTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return all orders for ADMIN")
        void shouldReturnAllOrders() throws Exception {
            // Given
            when(orderService.getAllOrders()).thenReturn(Arrays.asList(orderResponse1, orderResponse2));

            // When/Then
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("SHIPPED"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return empty list when no orders exist")
        void shouldReturnEmptyListWhenNoOrders() throws Exception {
            // Given
            when(orderService.getAllOrders()).thenReturn(Arrays.asList());

            // When/Then
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return orders with all item details")
        void shouldReturnOrdersWithAllItemDetails() throws Exception {
            // Given
            when(orderService.getAllOrders()).thenReturn(Arrays.asList(orderResponse1));

            // When/Then
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$[0].items[1].productName").value("Mouse"));
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("GET /api/admin/orders/{id} - Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return order by ID without ownership check")
        void shouldReturnOrderByIdWithoutOwnershipCheck() throws Exception {
            // Given
            when(orderService.getOrderByIdAdmin(1L)).thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(get("/api/admin/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(2049.97));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(orderService.getOrderByIdAdmin(999L))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

            // When/Then - GlobalExceptionHandler returns 404 NOT FOUND for OrderNotFoundException
            mockMvc.perform(get("/api/admin/orders/999")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return order with complete item details")
        void shouldReturnOrderWithCompleteItemDetails() throws Exception {
            // Given
            when(orderService.getOrderByIdAdmin(1L)).thenReturn(orderResponse1);

            // When/Then
            mockMvc.perform(get("/api/admin/orders/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].productId").value(101L))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].productImageUrl").value("laptop.jpg"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].priceAtPurchase").value(999.99))
                .andExpect(jsonPath("$.items[0].subtotal").value(1999.98));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should handle different order statuses")
        void shouldHandleDifferentOrderStatuses() throws Exception {
            // Given
            when(orderService.getOrderByIdAdmin(2L)).thenReturn(orderResponse2);

            // When/Then
            mockMvc.perform(get("/api/admin/orders/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("PUT /api/admin/orders/{id}/status - Update Order Status")
    class UpdateOrderStatusTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() throws Exception {
            // Given
            OrderResponse updatedOrder = new OrderResponse(
                1L, "ORD-20250128-00001", OrderStatus.PROCESSING,
                new BigDecimal("2049.97"), orderResponse1.getOrderedAt(), LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.PROCESSING)))
                .thenReturn(updatedOrder);

            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"));

            verify(orderService).updateOrderStatus(1L, OrderStatus.PROCESSING);
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(orderService.updateOrderStatus(eq(999L), eq(OrderStatus.PROCESSING)))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

            // When/Then - GlobalExceptionHandler returns 404 NOT FOUND for OrderNotFoundException
            mockMvc.perform(put("/api/admin/orders/999/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should handle status progression PENDING to PROCESSING")
        void shouldHandleStatusProgressionPendingToProcessing() throws Exception {
            // Given
            OrderResponse updatedOrder = new OrderResponse(
                1L, "ORD-20250128-00001", OrderStatus.PROCESSING,
                new BigDecimal("2049.97"), orderResponse1.getOrderedAt(), LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.updateOrderStatus(1L, OrderStatus.PROCESSING)).thenReturn(updatedOrder);

            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.PROCESSING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should handle status progression PROCESSING to SHIPPED")
        void shouldHandleStatusProgressionProcessingToShipped() throws Exception {
            // Given
            OrderResponse shippedOrder = new OrderResponse(
                1L, "ORD-20250128-00001", OrderStatus.SHIPPED,
                new BigDecimal("2049.97"), orderResponse1.getOrderedAt(), LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED)).thenReturn(shippedOrder);

            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.SHIPPED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should handle status progression SHIPPED to DELIVERED")
        void shouldHandleStatusProgressionShippedToDelivered() throws Exception {
            // Given
            OrderResponse deliveredOrder = new OrderResponse(
                1L, "ORD-20250128-00001", OrderStatus.DELIVERED,
                new BigDecimal("2049.97"), orderResponse1.getOrderedAt(), LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.updateOrderStatus(1L, OrderStatus.DELIVERED)).thenReturn(deliveredOrder);

            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.DELIVERED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return updated order with unchanged order number and total")
        void shouldReturnUpdatedOrderWithUnchangedFields() throws Exception {
            // Given
            OrderResponse updatedOrder = new OrderResponse(
                1L, "ORD-20250128-00001", OrderStatus.PROCESSING,
                new BigDecimal("2049.97"), orderResponse1.getOrderedAt(), LocalDateTime.now(),
                Arrays.asList(orderItem1, orderItem2)
            );
            when(orderService.updateOrderStatus(1L, OrderStatus.PROCESSING)).thenReturn(updatedOrder);

            // When/Then
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-20250128-00001"))
                .andExpect(jsonPath("$.totalAmount").value(2049.97))
                .andExpect(jsonPath("$.items", hasSize(2)));
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }

    @Nested
    @DisplayName("POST /api/admin/invalidate-all-tokens - Invalidate Tokens")
    class InvalidateTokensTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should invalidate all tokens successfully")
        void shouldInvalidateAllTokens() throws Exception {
            // Given
            doNothing().when(jwtUtil).invalidateAllTokens();

            // When/Then
            mockMvc.perform(post("/api/admin/invalidate-all-tokens")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.invalidatedAt").exists())
                .andExpect(jsonPath("$.success").value(true));

            verify(jwtUtil).invalidateAllTokens();
        }

        // Note: 403 tests for USER role tested in integration tests (Phase 5)
    }
}
