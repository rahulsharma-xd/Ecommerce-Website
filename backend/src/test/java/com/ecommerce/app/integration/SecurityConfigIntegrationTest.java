package com.ecommerce.app.integration;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for SecurityConfig endpoint security rules.
 * Tests cart and order endpoint authentication with real Spring Security context.
 *
 * Validates:
 * - /api/cart/** requires USER or ADMIN role
 * - /api/orders/** requires USER or ADMIN role
 * - /api/admin/orders/** requires ADMIN role (via /api/admin/**)
 * - Defense in depth: URL pattern + @PreAuthorize
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig Integration Test - Cart and Order Endpoints")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test (delete carts first to avoid FK constraint violation)
        cartRepository.deleteAll();
        userRepository.deleteAll();

        // Create USER
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("UserPass123!"));
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Create ADMIN
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("AdminPass123!"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // Login as USER to get token
        LoginRequest userLoginRequest = new LoginRequest();
        userLoginRequest.setEmail("user@example.com");
        userLoginRequest.setPassword("UserPass123!");

        MvcResult userLoginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse userLoginResponse = objectMapper.readValue(
            userLoginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        userToken = userLoginResponse.getToken();

        // Login as ADMIN to get token
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setPassword("AdminPass123!");

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse adminLoginResponse = objectMapper.readValue(
            adminLoginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        adminToken = adminLoginResponse.getToken();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test (delete carts first to avoid FK constraint violation)
        cartRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Cart Endpoint Security (/api/cart/**)")
    class CartEndpointSecurityTests {

        @Test
        @DisplayName("Should allow USER to access cart endpoints")
        void shouldAllowUserToAccessCartEndpoints() throws Exception {
            // GET cart
            mockMvc.perform(get("/api/cart")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow ADMIN to access cart endpoints")
        void shouldAllowAdminToAccessCartEndpoints() throws Exception {
            // GET cart
            mockMvc.perform(get("/api/cart")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // GET cart without token
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void shouldReturn401WithInvalidToken() throws Exception {
            // GET cart with invalid token
            mockMvc.perform(get("/api/cart")
                    .header("Authorization", "Bearer invalid.token.here")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow USER to add items to cart")
        void shouldAllowUserToAddItemsToCart() throws Exception {
            // POST add to cart - will fail with 500 (product not found) but security allows it
            String addToCartRequest = "{\"productId\": 1, \"quantity\": 2}";
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(addToCartRequest))
                .andExpect(status().isInternalServerError()); // 500 because product not found, not 401/403
        }

        @Test
        @DisplayName("Should allow USER to update cart item quantity")
        void shouldAllowUserToUpdateCartItemQuantity() throws Exception {
            // PUT update cart item - will fail with 500 (item not found) but security allows it
            String updateRequest = "{\"quantity\": 3}";
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                .andExpect(status().isInternalServerError()); // 500 because item not found, not 401/403
        }
    }

    @Nested
    @DisplayName("Order Endpoint Security (/api/orders/**)")
    class OrderEndpointSecurityTests {

        @Test
        @DisplayName("Should allow USER to access order endpoints")
        void shouldAllowUserToAccessOrderEndpoints() throws Exception {
            // GET orders
            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow ADMIN to access order endpoints")
        void shouldAllowAdminToAccessOrderEndpoints() throws Exception {
            // GET orders
            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // GET orders without token
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void shouldReturn401WithInvalidToken() throws Exception {
            // GET orders with invalid token
            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer invalid.token.here")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow USER to checkout")
        void shouldAllowUserToCheckout() throws Exception {
            // POST checkout - will fail with 400 (empty cart) but security allows it
            String checkoutRequest = "{\"cardNumber\": \"4532015112830366\", \"cardHolderName\": \"Test User\", \"expiryDate\": \"12/30\", \"cvv\": \"123\"}";
            mockMvc.perform(post("/api/orders/checkout")
                    .with(csrf())
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(checkoutRequest))
                .andExpect(status().isBadRequest()); // 400 because cart is empty, not 401/403
        }

        @Test
        @DisplayName("Should allow USER to get specific order")
        void shouldAllowUserToGetSpecificOrder() throws Exception {
            // GET order by ID - will fail with 404 (order not found) but security allows it
            mockMvc.perform(get("/api/orders/1")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 because order not found (GlobalExceptionHandler), not 401/403
        }
    }

    @Nested
    @DisplayName("Admin Order Endpoint Security (/api/admin/orders/**)")
    class AdminOrderEndpointSecurityTests {

        @Test
        @DisplayName("Should allow ADMIN to access admin order endpoints")
        void shouldAllowAdminToAccessAdminOrderEndpoints() throws Exception {
            // GET all orders (admin)
            mockMvc.perform(get("/api/admin/orders")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny USER access to admin order endpoints with 403")
        void shouldDenyUserAccessToAdminOrderEndpoints() throws Exception {
            // GET all orders (admin) - USER should get 403
            mockMvc.perform(get("/api/admin/orders")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // GET all orders without token
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow ADMIN to get specific order")
        void shouldAllowAdminToGetSpecificOrder() throws Exception {
            // GET order by ID (admin) - will fail with 404 (order not found) but security allows it
            mockMvc.perform(get("/api/admin/orders/1")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 because order not found (GlobalExceptionHandler), not 401/403
        }

        @Test
        @DisplayName("Should deny USER access to get specific order with 403")
        void shouldDenyUserAccessToGetSpecificOrder() throws Exception {
            // GET order by ID (admin) - USER should get 403
            mockMvc.perform(get("/api/admin/orders/1")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow ADMIN to update order status")
        void shouldAllowAdminToUpdateOrderStatus() throws Exception {
            // PUT update order status - will fail with 404 (order not found) but security allows it
            String statusRequest = "{\"status\": \"PROCESSING\"}";
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(statusRequest))
                .andExpect(status().isNotFound()); // 404 because order not found (GlobalExceptionHandler), not 401/403
        }

        @Test
        @DisplayName("Should deny USER access to update order status with 403")
        void shouldDenyUserAccessToUpdateOrderStatus() throws Exception {
            // PUT update order status - USER should get 403
            String statusRequest = "{\"status\": \"PROCESSING\"}";
            mockMvc.perform(put("/api/admin/orders/1/status")
                    .with(csrf())
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(statusRequest))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Defense in Depth - URL Pattern + @PreAuthorize")
    class DefenseInDepthTests {

        @Test
        @DisplayName("Cart endpoints protected by both URL pattern and @PreAuthorize")
        void cartEndpointsProtectedByBothMechanisms() throws Exception {
            // URL pattern: /api/cart/** requires hasAnyRole('USER', 'ADMIN')
            // @PreAuthorize: hasAnyRole('USER', 'ADMIN') on controller methods
            // Both mechanisms must allow access

            // USER should access cart
            mockMvc.perform(get("/api/cart")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // ADMIN should access cart
            mockMvc.perform(get("/api/cart")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // No token should get 401
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Order endpoints protected by both URL pattern and @PreAuthorize")
        void orderEndpointsProtectedByBothMechanisms() throws Exception {
            // URL pattern: /api/orders/** requires hasAnyRole('USER', 'ADMIN')
            // @PreAuthorize: hasAnyRole('USER', 'ADMIN') on controller methods
            // Both mechanisms must allow access

            // USER should access orders
            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // ADMIN should access orders
            mockMvc.perform(get("/api/orders")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // No token should get 401
            mockMvc.perform(get("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Admin order endpoints protected by both URL pattern and @PreAuthorize")
        void adminOrderEndpointsProtectedByBothMechanisms() throws Exception {
            // URL pattern: /api/admin/** requires hasRole('ADMIN')
            // @PreAuthorize: hasRole('ADMIN') on controller methods
            // Both mechanisms must allow access

            // ADMIN should access admin orders
            mockMvc.perform(get("/api/admin/orders")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

            // USER should get 403 (URL pattern blocks first)
            mockMvc.perform(get("/api/admin/orders")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

            // No token should get 401
            mockMvc.perform(get("/api/admin/orders")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Public Endpoints - No Authentication Required")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should allow unauthenticated access to public auth endpoints")
        void shouldAllowUnauthenticatedAccessToAuthEndpoints() throws Exception {
            // Login endpoint should be public
            String loginRequest = "{\"email\": \"user@example.com\", \"password\": \"WrongPass123!\"}";
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                .andExpect(status().isUnauthorized()); // 401 because wrong password, not 403
        }

        @Test
        @DisplayName("Should allow unauthenticated access to public product endpoints")
        void shouldAllowUnauthenticatedAccessToProductEndpoints() throws Exception {
            // Product endpoints should be public
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }
}
