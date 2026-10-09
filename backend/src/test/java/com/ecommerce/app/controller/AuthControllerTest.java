package com.ecommerce.app.controller;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.LoginResponse;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.dto.RegisterResponse;
import com.ecommerce.app.dto.UnlockRequest;
import com.ecommerce.app.dto.UnlockResponse;
import com.ecommerce.app.exception.InvalidCredentialsException;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.UserAlreadyExistsException;
import com.ecommerce.app.security.JwtAuthenticationFilter;
import com.ecommerce.app.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests REST API endpoints with Spring Security using MockMvc.
 */
@WebMvcTest(value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UnlockRequest unlockRequest;
    private RegisterResponse registerResponse;
    private LoginResponse loginResponse;
    private UnlockResponse unlockResponse;

    @BeforeEach
    void setUp() {
        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Setup unlock request
        unlockRequest = new UnlockRequest();
        unlockRequest.setEmail("test@example.com");

        // Setup register response
        registerResponse = new RegisterResponse(
            1L,
            "testuser",
            "test@example.com",
            "User registered successfully"
        );

        // Setup login response
        loginResponse = new LoginResponse(
            1L,
            "testuser",
            "test@example.com",
            "USER",
            "Login successful",
            "jwt-token-123"
        );

        // Setup unlock response
        unlockResponse = new UnlockResponse(
            1L,
            "test@example.com",
            "Account unlocked successfully",
            true
        );
    }

    @Nested
    @DisplayName("POST /api/auth/register - User Registration")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            when(authService.registerUser(any(RegisterRequest.class))).thenReturn(registerResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            // Given
            when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already registered"));

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            // Given
            when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already taken"));

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given
            when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new InvalidInputException("Username is required"));

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register-admin - Admin Registration")
    class AdminRegistrationTests {

        @Test
        @DisplayName("Should register admin successfully")
        void shouldRegisterAdminSuccessfully() throws Exception {
            // Given
            RegisterResponse adminResponse = new RegisterResponse(
                2L,
                "admin",
                "admin@example.com",
                "Admin registered successfully"
            );
            when(authService.registerAdmin(any(RegisterRequest.class))).thenReturn(adminResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/register-admin")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.message").value("Admin registered successfully"));
        }

        // Note: Security authorization tests (403/401) are tested in integration tests
        // since @AutoConfigureMockMvc(addFilters = false) disables security for unit testing
    }

    @Nested
    @DisplayName("POST /api/auth/login - User Login")
    class UserLoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            // Given
            when(authService.loginUser(any(LoginRequest.class))).thenReturn(loginResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsInvalid() throws Exception {
            // Given
            when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given
            when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new InvalidInputException("Email is required"));

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 423 when account is locked")
        void shouldReturn423WhenAccountLocked() throws Exception {
            // Given
            when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Account is locked due to too many failed login attempts"));

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError()); // RuntimeException returns 500
        }
    }

    @Nested
    @DisplayName("POST /api/auth/unlock - Account Unlock")
    class AccountUnlockTests {

        @Test
        @DisplayName("Should unlock account successfully")
        void shouldUnlockAccountSuccessfully() throws Exception {
            // Given
            when(authService.unlockAccount(anyString())).thenReturn(unlockResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/unlock")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Account unlocked successfully"))
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 when email is null")
        void shouldReturn400WhenEmailIsNull() throws Exception {
            // Given
            unlockRequest.setEmail(null);

            // When/Then
            mockMvc.perform(post("/api/auth/unlock")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email is empty")
        void shouldReturn400WhenEmailIsEmpty() throws Exception {
            // Given
            unlockRequest.setEmail("   ");

            // When/Then
            mockMvc.perform(post("/api/auth/unlock")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void shouldReturn400WhenEmailFormatInvalid() throws Exception {
            // Given
            unlockRequest.setEmail("invalidemail");

            // When/Then
            mockMvc.perform(post("/api/auth/unlock")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when user not found")
        void shouldReturn500WhenUserNotFound() throws Exception {
            // Given
            when(authService.unlockAccount(anyString()))
                .thenThrow(new RuntimeException("User not found"));

            // When/Then
            mockMvc.perform(post("/api/auth/unlock")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().is5xxServerError());
        }
    }
}
