package com.ecommerce.app.integration;

import com.ecommerce.app.dto.*;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
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
 * Integration test for complete admin workflow.
 * Tests: Admin login → Create product → Update product → Delete product
 *
 * Uses @SpringBootTest for full application context with real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin Workflow Integration Test")
class AdminWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user manually
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(adminUser);

        // Login as admin to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("AdminPass123!");

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
        adminToken = loginResponse.getToken();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full admin workflow: Create → Update → Delete product")
    void shouldCompleteFullAdminWorkflow() throws Exception {
        // Step 1: Create a new product
        ProductRequest createRequest = new ProductRequest();
        createRequest.setName("Gaming Laptop");
        createRequest.setDescription("High-end gaming laptop");
        createRequest.setPrice(new BigDecimal("1999.99"));
        createRequest.setStockQuantity(10);
        createRequest.setCategory("Electronics");
        createRequest.setBrand("ASUS");
        createRequest.setImageUrl("gaming-laptop.jpg");

        MvcResult createResult = mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Gaming Laptop"))
            .andExpect(jsonPath("$.price").value(1999.99))
            .andExpect(jsonPath("$.stockQuantity").value(10))
            .andExpect(jsonPath("$.active").value(true))
            .andReturn();

        ProductResponse createdProduct = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProductResponse.class
        );
        Long productId = createdProduct.getId();

        assertThat(productId).isNotNull();
        assertThat(productRepository.findById(productId)).isPresent();

        // Step 2: Update the product
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Gaming Laptop Pro");
        updateRequest.setPrice(new BigDecimal("2299.99"));
        updateRequest.setStockQuantity(15);

        mockMvc.perform(put("/api/admin/products/" + productId)
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Gaming Laptop Pro"))
            .andExpect(jsonPath("$.price").value(2299.99))
            .andExpect(jsonPath("$.stockQuantity").value(15));

        // Verify update in database
        assertThat(productRepository.findById(productId).get().getName())
            .isEqualTo("Gaming Laptop Pro");

        // Step 3: Delete the product (soft delete)
        mockMvc.perform(delete("/api/admin/products/" + productId)
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.deleted").value(true));

        // Verify product is soft deleted (marked as inactive)
        assertThat(productRepository.findById(productId).get().getActive())
            .isFalse();
    }

    @Test
    @DisplayName("Should deny regular USER access to admin endpoints")
    void shouldDenyUserAccessToAdminEndpoints() throws Exception {
        // Create a regular user
        User regularUser = new User();
        regularUser.setUsername("regularuser");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("UserPass123!"));
        regularUser.setRole(Role.USER);
        regularUser.setActive(true);
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(regularUser);

        // Login as regular user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("UserPass123!");

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
        String userToken = loginResponse.getToken();

        // Try to create product as regular user - should be forbidden
        ProductRequest createRequest = new ProductRequest();
        createRequest.setName("Test Product");
        createRequest.setDescription("Test");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setStockQuantity(5);
        createRequest.setCategory("Test");

        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to view all products including inactive")
    void shouldAllowAdminToViewAllProducts() throws Exception {
        // Create active product
        ProductRequest activeProduct = new ProductRequest();
        activeProduct.setName("Active Product");
        activeProduct.setDescription("Active");
        activeProduct.setPrice(new BigDecimal("100.00"));
        activeProduct.setStockQuantity(10);
        activeProduct.setCategory("Test");

        MvcResult createResult = mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activeProduct)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Soft delete the product
        mockMvc.perform(delete("/api/admin/products/" + created.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Admin should see both active and inactive products
        mockMvc.perform(get("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(created.getId()))
            .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    @DisplayName("Should allow admin to get all users")
    void shouldAllowAdminToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].email").value("admin@example.com"));
    }

    @Test
    @DisplayName("Should allow admin to delete users")
    void shouldAllowAdminToDeleteUsers() throws Exception {
        // Create a user to delete
        User userToDelete = new User();
        userToDelete.setUsername("deleteuser");
        userToDelete.setEmail("delete@example.com");
        userToDelete.setPassword(passwordEncoder.encode("Pass123!"));
        userToDelete.setRole(Role.USER);
        userToDelete.setActive(true);
        userToDelete.setCreatedAt(LocalDateTime.now());
        userToDelete.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(userToDelete);

        // Delete the user
        mockMvc.perform(delete("/api/admin/users/" + saved.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.deleted").value(true));

        // Verify user is deleted from database
        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    // Note: Token invalidation test removed as it affects other tests in the suite
    // Token invalidation is tested in AccountSecurityIntegrationTest in isolation

    @Test
    @DisplayName("Should prevent product creation with invalid data")
    void shouldPreventProductCreationWithInvalidData() throws Exception {
        ProductRequest invalidProduct = new ProductRequest();
        invalidProduct.setName("");  // Empty name
        invalidProduct.setDescription("Test");
        invalidProduct.setPrice(new BigDecimal("-10.00"));  // Negative price
        invalidProduct.setStockQuantity(-5);  // Negative stock

        mockMvc.perform(post("/api/admin/products")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
            .andExpect(status().isBadRequest());
    }
}
