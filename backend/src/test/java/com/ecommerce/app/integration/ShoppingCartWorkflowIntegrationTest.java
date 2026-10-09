package com.ecommerce.app.integration;

import com.ecommerce.app.dto.*;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for complete shopping cart workflow.
 * Tests: Browse products → Add to cart → Update quantities → Verify totals
 *
 * Uses @SpringBootTest for full application context with real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Shopping Cart Workflow Integration Test")
class ShoppingCartWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test products
        product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("High performance laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setStockQuantity(50);
        product1.setCategory("Electronics");
        product1.setBrand("Dell");
        product1.setImageUrl("laptop.jpg");
        product1.setActive(true);
        product1.setCreatedAt(LocalDateTime.now());
        product1.setUpdatedAt(LocalDateTime.now());
        product1 = productRepository.save(product1);

        product2 = new Product();
        product2.setName("Mouse");
        product2.setDescription("Wireless mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setStockQuantity(100);
        product2.setCategory("Electronics");
        product2.setBrand("Logitech");
        product2.setImageUrl("mouse.jpg");
        product2.setActive(true);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());
        product2 = productRepository.save(product2);

        product3 = new Product();
        product3.setName("Keyboard");
        product3.setDescription("Mechanical keyboard");
        product3.setPrice(new BigDecimal("79.99"));
        product3.setStockQuantity(30);
        product3.setCategory("Electronics");
        product3.setBrand("Razer");
        product3.setImageUrl("keyboard.jpg");
        product3.setActive(true);
        product3.setCreatedAt(LocalDateTime.now());
        product3.setUpdatedAt(LocalDateTime.now());
        product3 = productRepository.save(product3);

        // Create regular user
        User user = new User();
        user.setUsername("shopper");
        user.setEmail("shopper@example.com");
        user.setPassword(passwordEncoder.encode("ShopperPass123!"));
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("shopper@example.com");
        loginRequest.setPassword("ShopperPass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        userToken = loginResponse.getToken();
    }

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full shopping cart workflow: Browse → Add → Update → Verify")
    void shouldCompleteFullShoppingCartWorkflow() throws Exception {
        // Step 1: Browse products (public endpoint)
        MvcResult browseResult = mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andReturn();

        // Step 2: View specific product details
        mockMvc.perform(get("/api/products/" + product1.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.price").value(999.99))
            .andExpect(jsonPath("$.stockQuantity").value(50));

        // Step 3: Get empty cart (should auto-create)
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isEmpty())
            .andExpect(jsonPath("$.totalItems").value(0))
            .andExpect(jsonPath("$.totalPrice").value(0));

        // Step 4: Add first product to cart
        AddToCartRequest addLaptop = new AddToCartRequest(product1.getId(), 2);
        MvcResult addResult1 = mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addLaptop)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andExpect(jsonPath("$.items[0].priceAtAddition").value(999.99))
            .andExpect(jsonPath("$.items[0].subtotal").value(1999.98))
            .andExpect(jsonPath("$.totalItems").value(2))
            .andExpect(jsonPath("$.totalPrice").value(1999.98))
            .andReturn();

        CartDTO cart1 = objectMapper.readValue(
            addResult1.getResponse().getContentAsString(),
            CartDTO.class
        );
        Long laptopCartItemId = cart1.getItems().get(0).getId();

        // Step 5: Add second product to cart
        AddToCartRequest addMouse = new AddToCartRequest(product2.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addMouse)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.totalItems").value(3))
            .andExpect(jsonPath("$.totalPrice").value(2029.97));

        // Step 6: Add third product to cart
        AddToCartRequest addKeyboard = new AddToCartRequest(product3.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addKeyboard)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.totalItems").value(4))
            .andExpect(jsonPath("$.totalPrice").value(2109.96));

        // Step 7: Update laptop quantity from 2 to 3
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(3);
        mockMvc.perform(put("/api/cart/items/" + laptopCartItemId)
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].quantity").value(3))
            .andExpect(jsonPath("$.items[0].subtotal").value(2999.97))
            .andExpect(jsonPath("$.totalItems").value(5))
            .andExpect(jsonPath("$.totalPrice").value(3109.95));

        // Step 8: Verify final cart state
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.totalItems").value(5))
            .andExpect(jsonPath("$.totalPrice").value(3109.95));
    }

    @Test
    @DisplayName("Should add same product twice and increase quantity")
    void shouldIncreaseQuantityWhenAddingSameProduct() throws Exception {
        // Add product first time
        AddToCartRequest addRequest1 = new AddToCartRequest(product1.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].quantity").value(1));

        // Add same product again
        AddToCartRequest addRequest2 = new AddToCartRequest(product1.getId(), 2);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].quantity").value(3))
            .andExpect(jsonPath("$.items[0].subtotal").value(2999.97));
    }

    @Test
    @DisplayName("Should prevent adding more items than available stock")
    void shouldPreventExceedingStock() throws Exception {
        // Try to add more than available stock (keyboard has 30 in stock)
        AddToCartRequest excessiveRequest = new AddToCartRequest(product3.getId(), 31);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(excessiveRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should prevent updating cart item quantity beyond stock")
    void shouldPreventUpdatingBeyondStock() throws Exception {
        // Add product to cart
        AddToCartRequest addRequest = new AddToCartRequest(product3.getId(), 10);
        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk())
            .andReturn();

        CartDTO cart = objectMapper.readValue(
            addResult.getResponse().getContentAsString(),
            CartDTO.class
        );
        Long itemId = cart.getItems().get(0).getId();

        // Try to update to exceed stock (keyboard has 30 in stock)
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(31);
        mockMvc.perform(put("/api/cart/items/" + itemId)
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should prevent adding inactive products to cart")
    void shouldPreventAddingInactiveProducts() throws Exception {
        // Mark product as inactive
        product1.setActive(false);
        productRepository.save(product1);

        // Try to add inactive product
        AddToCartRequest addRequest = new AddToCartRequest(product1.getId(), 1);
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should prevent user from updating another user's cart items")
    void shouldPreventCrossUserCartAccess() throws Exception {
        // Create second user
        User user2 = new User();
        user2.setUsername("shopper2");
        user2.setEmail("shopper2@example.com");
        user2.setPassword(passwordEncoder.encode("ShopperPass123!"));
        user2.setRole(Role.USER);
        user2.setActive(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user2);

        // First user adds item to cart
        AddToCartRequest addRequest = new AddToCartRequest(product1.getId(), 1);
        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk())
            .andReturn();

        CartDTO cart = objectMapper.readValue(
            addResult.getResponse().getContentAsString(),
            CartDTO.class
        );
        Long itemId = cart.getItems().get(0).getId();

        // Login as second user
        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setEmail("shopper2@example.com");
        loginRequest2.setPassword("ShopperPass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest2)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        String user2Token = loginResponse.getToken();

        // Second user tries to update first user's cart item
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(10);
        mockMvc.perform(put("/api/cart/items/" + itemId)
                .with(csrf())
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should calculate correct totals with multiple items")
    void shouldCalculateCorrectTotals() throws Exception {
        // Add multiple products with different quantities
        AddToCartRequest add1 = new AddToCartRequest(product1.getId(), 2); // 2 * 999.99 = 1999.98
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(add1)))
            .andExpect(status().isOk());

        AddToCartRequest add2 = new AddToCartRequest(product2.getId(), 3); // 3 * 29.99 = 89.97
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(add2)))
            .andExpect(status().isOk());

        AddToCartRequest add3 = new AddToCartRequest(product3.getId(), 1); // 1 * 79.99 = 79.99
        mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(add3)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(6))
            .andExpect(jsonPath("$.totalPrice").value(2169.94));
    }

    @Test
    @DisplayName("Should preserve price at addition time")
    void shouldPreservePriceAtAddition() throws Exception {
        // Add product to cart
        AddToCartRequest addRequest = new AddToCartRequest(product1.getId(), 1);
        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].priceAtAddition").value(999.99))
            .andReturn();

        // Change product price in database
        product1.setPrice(new BigDecimal("1299.99"));
        productRepository.save(product1);

        // Retrieve cart - price should remain the same (price at addition)
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].priceAtAddition").value(999.99))
            .andExpect(jsonPath("$.items[0].subtotal").value(999.99));
    }
}
