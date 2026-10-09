package com.ecommerce.app.controller;

import com.ecommerce.app.dto.DeleteResponse;
import com.ecommerce.app.dto.InvalidateTokensResponse;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.dto.ProductResponse;
import com.ecommerce.app.dto.UpdateOrderStatusRequest;
import com.ecommerce.app.dto.UserResponse;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtUtil;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Debug endpoint to check current user's authorities
     * Helps troubleshoot authorization issues
     */
    @GetMapping("/debug-auth")
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("message", "No authentication found"));
        }

        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "authorities", authorities,
            "authenticated", authentication.isAuthenticated()
        ));
    }

    /**
     * ADMIN-only test endpoint
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome Admin! This endpoint is accessible only by ADMIN role",
            "role", "ADMIN"
        ));
    }

    /**
     * Get all users - ADMIN only
     * Returns list of all registered users
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /**
     * Delete user by ID - ADMIN only
     * Validates user exists before deletion
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param id the ID of the user to delete
     * @return DeleteResponse with success message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<DeleteResponse> deleteUser(@PathVariable Long id) {
        // Validate user exists
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Delete the user
        userRepository.deleteById(id);

        // Return success response
        DeleteResponse response = new DeleteResponse(
            id,
            "User '" + user.getUsername() + "' deleted successfully",
            true
        );

        return ResponseEntity.ok(response);
    }

    // ==================== PRODUCT MANAGEMENT ENDPOINTS ====================

    /**
     * Get all products including inactive ones - ADMIN only
     * Returns all products in the database (active and inactive)
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @return List of all products
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Create new product - ADMIN only
     * Validates product data before creation
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param request ProductRequest with product details
     * @return Created product details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update existing product - ADMIN only
     * Supports partial updates (only updates provided fields)
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param id Product ID to update
     * @param request ProductRequest with updated fields
     * @return Updated product details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Soft delete product - ADMIN only
     * Marks product as inactive instead of deleting from database
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param id Product ID to delete
     * @return DeleteResponse with success message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<DeleteResponse> deleteProduct(@PathVariable Long id) {
        // Get product details before deletion for response
        ProductResponse product = productService.getProductById(id);

        // Perform soft delete
        productService.deleteProduct(id);

        // Return success response
        DeleteResponse response = new DeleteResponse(
            id,
            "Product '" + product.getName() + "' deleted successfully (soft delete)",
            true
        );

        return ResponseEntity.ok(response);
    }

    // ==================== ORDER MANAGEMENT ENDPOINTS ====================

    /**
     * Get all orders - ADMIN only
     * Returns all orders in the system ordered by most recent first
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @return List of all orders
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID - ADMIN only
     * Returns any order without ownership check
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param id Order ID
     * @return Order details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderByIdAdmin(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Update order status - ADMIN only
     * Updates shipping status (PENDING → PROCESSING → SHIPPED → DELIVERED)
     * Only accessible by users with ADMIN role
     * USER role will receive 403 Forbidden
     *
     * @param id Order ID
     * @param request UpdateOrderStatusRequest with new status
     * @return Updated order details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse order = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }

    // ==================== SECURITY MANAGEMENT ENDPOINTS ====================

    /**
     * Invalidate all JWT tokens globally - ADMIN only
     * Forces all users (including admin) to re-authenticate
     *
     * WARNING: This will invalidate YOUR current token too!
     * You will need to login again after calling this endpoint.
     *
     * Use cases:
     * - Security breach response
     * - Force all users to re-login
     * - Clear all active sessions
     *
     * @return InvalidateTokensResponse with timestamp and message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invalidate-all-tokens")
    public ResponseEntity<InvalidateTokensResponse> invalidateAllTokens() {
        // Invalidate all tokens
        jwtUtil.invalidateAllTokens();

        // Create response
        InvalidateTokensResponse response = new InvalidateTokensResponse(
            "All JWT tokens have been invalidated. All users (including you) must login again.",
            LocalDateTime.now(),
            true
        );

        return ResponseEntity.ok(response);
    }
}
