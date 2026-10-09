package com.ecommerce.app.integration;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.dto.RegisterResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for complete user registration and login workflow.
 * Tests the entire flow: Register → Login → Access protected endpoint
 *
 * Uses @SpringBootTest for full application context with real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Registration and Login Integration Test")
class UserRegistrationAndLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();

        // Setup registration request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationuser");
        registerRequest.setEmail("integration@example.com");
        registerRequest.setPassword("SecurePass123!");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("integration@example.com");
        loginRequest.setPassword("SecurePass123!");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full user journey: Register → Login → Access Dashboard")
    void shouldCompleteFullUserJourney() throws Exception {
        // Step 1: Register a new user
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("integrationuser"))
            .andExpect(jsonPath("$.email").value("integration@example.com"))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andReturn();

        String registerResponseJson = registerResult.getResponse().getContentAsString();
        RegisterResponse registerResponse = objectMapper.readValue(registerResponseJson, RegisterResponse.class);

        assertThat(registerResponse.getId()).isNotNull();
        assertThat(registerResponse.getUsername()).isEqualTo("integrationuser");

        // Verify user exists in database
        assertThat(userRepository.findByEmail("integration@example.com")).isPresent();

        // Step 2: Login with the registered credentials
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("integrationuser"))
            .andExpect(jsonPath("$.email").value("integration@example.com"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseJson, LoginResponse.class);

        String jwtToken = loginResponse.getToken();
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();

        // Step 3: Access protected endpoint with JWT token
        mockMvc.perform(get("/api/user/dashboard")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Welcome to your dashboard!"))
            .andExpect(jsonPath("$.email").value("integration@example.com"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    @DisplayName("Should deny access to protected endpoint without token")
    void shouldDenyAccessWithoutToken() throws Exception {
        // Register user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Try to access protected endpoint without token
        mockMvc.perform(get("/api/user/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with invalid token")
    void shouldDenyAccessWithInvalidToken() throws Exception {
        // Try to access protected endpoint with invalid token
        mockMvc.perform(get("/api/user/dashboard")
                .header("Authorization", "Bearer invalid.token.here")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should prevent duplicate registration with same email")
    void shouldPreventDuplicateRegistration() throws Exception {
        // Register user first time
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Try to register again with same email
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should fail login with incorrect password")
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        // Register user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        // Try to login with wrong password
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail("integration@example.com");
        wrongPasswordRequest.setPassword("WrongPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail login with non-existent email")
    void shouldFailLoginWithNonExistentEmail() throws Exception {
        // Try to login without registering
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to public endpoints without authentication")
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Public endpoint: Get all products
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should complete user journey with role information access")
    void shouldAccessRoleInformationAfterLogin() throws Exception {
        // Register and login
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

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
        String jwtToken = loginResponse.getToken();

        // Access role information endpoint
        mockMvc.perform(get("/api/user/role-info")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("integration@example.com"))
            .andExpect(jsonPath("$.user").value(true))
            .andExpect(jsonPath("$.admin").value(false));
    }
}
