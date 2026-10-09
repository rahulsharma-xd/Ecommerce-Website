package com.ecommerce.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom handler for authentication errors (401 Unauthorized)
 * Triggered when unauthenticated user tries to access protected resource
 * Returns JSON response instead of default HTML error page
 *
 * Common scenarios:
 * - No JWT token provided
 * - Invalid JWT token
 * - Expired JWT token
 * - Malformed Authorization header
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Set response status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Create custom error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication is required to access this resource. Please provide a valid JWT token.");
        errorResponse.put("path", request.getRequestURI());

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
