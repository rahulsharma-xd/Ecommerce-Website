package com.ecommerce.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom handler for access denied exceptions (403 Forbidden)
 * Triggered when authenticated user lacks required role/permission
 * This handles Spring Security filter-level access denials (e.g., @PreAuthorize failures)
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Set response status to 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        // Create custom error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 403);
        errorResponse.put("message", "Access denied. You do not have the required permissions to access this resource.");

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
