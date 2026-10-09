package com.ecommerce.app.controller;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for order management.
 * Handles checkout, order history, and order details for authenticated users.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Process checkout with cart items
     * Creates order, deducts stock, clears cart, and sends confirmation email
     * Accessible by both USER and ADMIN roles
     *
     * @param authentication current authentication context
     * @param request checkout request with card details
     * @return order response with order details and order number
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request) {
        String email = getCurrentUserEmail(authentication);
        OrderResponse order = orderService.checkout(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get authenticated user's order history
     * Returns all orders for the current user, ordered by most recent first
     * Accessible by both USER and ADMIN roles
     *
     * @param authentication current authentication context
     * @return list of user's orders
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication authentication) {
        String email = getCurrentUserEmail(authentication);
        List<OrderResponse> orders = orderService.getUserOrders(email);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get specific order details by ID
     * Validates that the order belongs to the authenticated user
     * Accessible by both USER and ADMIN roles
     *
     * @param authentication current authentication context
     * @param id order ID
     * @return order details
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            Authentication authentication,
            @PathVariable Long id) {
        String email = getCurrentUserEmail(authentication);
        OrderResponse order = orderService.getOrderById(email, id);
        return ResponseEntity.ok(order);
    }

    /**
     * Helper method to extract email from authentication context
     *
     * @param authentication current authentication context
     * @return user's email
     */
    private String getCurrentUserEmail(Authentication authentication) {
        return authentication.getName();
    }
}
