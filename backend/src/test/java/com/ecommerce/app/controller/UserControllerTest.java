package com.ecommerce.app.controller;

import com.ecommerce.app.dto.RoleInfoResponse;
import com.ecommerce.app.dto.UserResponse;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtAuthenticationFilter;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * Tests user-specific REST API endpoints with role-based access control.
 *
 * Note: Fine-grained @PreAuthorize authorization (e.g., "authentication.name == #email")
 * requires full Spring Security context and is tested in integration tests (Phase 5).
 * These unit tests focus on functional behavior with proper credentials.
 */
@WebMvcTest(value = UserController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;
    private UserResponse userResponse;

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
    }

    @Nested
    @DisplayName("GET /api/user/user-only - User Only Endpoint")
    class UserOnlyTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should allow USER access")
        void shouldAllowUserAccess() throws Exception {
            mockMvc.perform(get("/api/user/user-only")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.role").value("USER"));
        }

        // Note: 403 Forbidden tests for ADMIN role require full Security context
        // and are tested in integration tests (Phase 5)

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/user/user-only")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/user/dashboard - Dashboard Endpoint")
    class DashboardTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return dashboard for USER")
        void shouldReturnDashboardForUser() throws Exception {
            mockMvc.perform(get("/api/user/dashboard")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to your dashboard!"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.authenticated").value(true));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return dashboard for ADMIN")
        void shouldReturnDashboardForAdmin() throws Exception {
            mockMvc.perform(get("/api/user/dashboard")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to your dashboard!"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.authenticated").value(true));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/user/dashboard")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/user/role-info - Role Information")
    class RoleInfoTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return role info for USER")
        void shouldReturnRoleInfoForUser() throws Exception {
            mockMvc.perform(get("/api/user/role-info")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.authorities").isArray())
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.user").value(true));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should return role info for ADMIN")
        void shouldReturnRoleInfoForAdmin() throws Exception {
            mockMvc.perform(get("/api/user/role-info")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.authorities").isArray())
                .andExpect(jsonPath("$.admin").value(true))
                .andExpect(jsonPath("$.user").value(false));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/user/role-info")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/user/profile/{email} - Get User Profile")
    class GetProfileTests {

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should allow USER to view own profile")
        void shouldAllowUserToViewOwnProfile() throws Exception {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            mockMvc.perform(get("/api/user/profile/test@example.com")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow ADMIN to view any profile")
        void shouldAllowAdminToViewAnyProfile() throws Exception {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            mockMvc.perform(get("/api/user/profile/test@example.com")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        @WithMockUser(username = "test@example.com", roles = {"USER"})
        @DisplayName("Should return 500 when user not found")
        void shouldReturn500WhenUserNotFound() throws Exception {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/user/profile/nonexistent@example.com")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        }

        // Note: 403 Forbidden test (USER accessing another user's profile)
        // requires full Spring Security SpEL evaluation and is tested in
        // integration tests (Phase 5)

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/user/profile/test@example.com")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }
}
