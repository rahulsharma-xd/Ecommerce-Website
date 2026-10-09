package com.ecommerce.app.controller;

import com.ecommerce.app.dto.AddToCartRequest;
import com.ecommerce.app.dto.CartDTO;
import com.ecommerce.app.dto.CartItemDTO;
import com.ecommerce.app.dto.UpdateCartItemRequest;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.security.JwtAuthenticationFilter;
import com.ecommerce.app.service.CartService;
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
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CartController.
 * Tests REST API endpoints with Spring Security using MockMvc.
 *
 * Note: Since CartController requires authentication, we use @WithMockUser
 * and disable JWT filter to focus on functional testing.
 */
@WebMvcTest(value = CartController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc
@DisplayName("CartController Integration Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartDTO emptyCart;
    private CartDTO cartWithItems;
    private CartItemDTO cartItem1;
    private CartItemDTO cartItem2;
    private AddToCartRequest addToCartRequest;
    private UpdateCartItemRequest updateCartItemRequest;

    @BeforeEach
    void setUp() {
        // Setup empty cart
        emptyCart = new CartDTO(1L, Collections.emptyList());

        // Setup cart items
        cartItem1 = new CartItemDTO(
            1L,
            101L,
            "Laptop",
            "laptop.jpg",
            new BigDecimal("999.99"),
            2
        );

        cartItem2 = new CartItemDTO(
            2L,
            102L,
            "Mouse",
            "mouse.jpg",
            new BigDecimal("49.99"),
            1
        );

        // Setup cart with items
        cartWithItems = new CartDTO(1L, Arrays.asList(cartItem1, cartItem2));

        // Setup requests
        addToCartRequest = new AddToCartRequest(101L, 2);
        updateCartItemRequest = new UpdateCartItemRequest(3);
    }

    @Nested
    @DisplayName("GET /api/cart - Get User's Cart")
    class GetCartTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return empty cart when user has no items")
        void shouldReturnEmptyCart() throws Exception {
            // Given
            when(cartService.getCart(anyString())).thenReturn(emptyCart);

            // When/Then
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return cart with items successfully")
        void shouldReturnCartWithItems() throws Exception {
            // Given
            when(cartService.getCart(anyString())).thenReturn(cartWithItems);

            // When/Then
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(2049.97))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].productId").value(101L))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(1999.98))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.items[1].productName").value("Mouse"));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to access their cart")
        void shouldAllowAdminAccess() throws Exception {
            // Given
            when(cartService.getCart(anyString())).thenReturn(emptyCart);

            // When/Then
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/cart/items - Add Item to Cart")
    class AddToCartTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should add item to cart successfully")
        void shouldAddItemToCart() throws Exception {
            // Given
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenReturn(cartWithItems);

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalItems").value(3));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when product not found")
        void shouldReturn400WhenProductNotFound() throws Exception {
            // Given
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenThrow(new InvalidInputException("Product not found"));

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when insufficient stock")
        void shouldReturn400WhenInsufficientStock() throws Exception {
            // Given
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenThrow(new InvalidInputException("Insufficient stock"));

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when product is inactive")
        void shouldReturn400WhenProductInactive() throws Exception {
            // Given
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenThrow(new InvalidInputException("Product is not available"));

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when quantity is invalid")
        void shouldReturn400WhenQuantityInvalid() throws Exception {
            // Given
            AddToCartRequest invalidRequest = new AddToCartRequest(101L, 0);
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenThrow(new InvalidInputException("Quantity must be at least 1"));

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to add items to cart")
        void shouldAllowAdminToAddItems() throws Exception {
            // Given
            when(cartService.addToCart(anyString(), any(AddToCartRequest.class)))
                .thenReturn(cartWithItems);

            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/cart/items/{itemId} - Update Cart Item")
    class UpdateCartItemTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should update cart item quantity successfully")
        void shouldUpdateCartItemQuantity() throws Exception {
            // Given
            CartItemDTO updatedItem = new CartItemDTO(
                1L, 101L, "Laptop", "laptop.jpg", new BigDecimal("999.99"), 3
            );
            CartDTO updatedCart = new CartDTO(1L, Arrays.asList(updatedItem, cartItem2));
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenReturn(updatedCart);

            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.items[0].subtotal").value(2999.97));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when cart item not found")
        void shouldReturn400WhenCartItemNotFound() throws Exception {
            // Given
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidInputException("Cart item not found"));

            // When/Then
            mockMvc.perform(put("/api/cart/items/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when insufficient stock for update")
        void shouldReturn400WhenInsufficientStockForUpdate() throws Exception {
            // Given
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidInputException("Insufficient stock"));

            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "wronguser@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when user doesn't own cart item")
        void shouldReturn400WhenUserDoesntOwnCartItem() throws Exception {
            // Given
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidInputException("Cart item does not belong to user"));

            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 400 when quantity is invalid")
        void shouldReturn400WhenUpdateQuantityInvalid() throws Exception {
            // Given
            UpdateCartItemRequest invalidRequest = new UpdateCartItemRequest(0);
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidInputException("Quantity must be at least 1"));

            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to update cart items")
        void shouldAllowAdminToUpdateItems() throws Exception {
            // Given
            when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenReturn(cartWithItems);

            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When/Then
            mockMvc.perform(put("/api/cart/items/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should handle invalid item ID format gracefully")
        void shouldHandleInvalidItemIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(put("/api/cart/items/invalid")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().is5xxServerError());
        }
    }
}
