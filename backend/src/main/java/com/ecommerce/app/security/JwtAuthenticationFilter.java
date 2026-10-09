package com.ecommerce.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts HTTP requests and validates JWT tokens.
 * Executes once per request to extract and validate token from Authorization header.
 * If token is valid, sets authentication in Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Filters each request to check for valid JWT token.
     * Extracts token from "Authorization: Bearer <token>" header.
     * Validates token and sets authentication if valid.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain to proceed
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extract JWT token from Authorization header
            String jwt = extractTokenFromRequest(request);

            // If token exists and is valid, set authentication
            if (jwt != null && jwtUtil.validateToken(jwt, extractEmailFromToken(jwt))) {
                // Extract email from token
                String email = extractEmailFromToken(jwt);

                // Load user details from database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                // Add request details for better security
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log error but don't fail - allow request to proceed
            // (unauthorized endpoint handlers will deny access)
            logger.error("Cannot set user authentication in security context", ex);
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from Authorization header.
     * Expected format: "Authorization: Bearer <token>"
     *
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Get Authorization header
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Extracts email from JWT token.
     * Token subject (sub) contains the user's email.
     *
     * @param token JWT token
     * @return email extracted from token
     */
    private String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
}
