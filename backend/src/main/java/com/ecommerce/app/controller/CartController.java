package com.ecommerce.app.controller;

import com.ecommerce.app.dto.AddToCartRequest;
import com.ecommerce.app.dto.CartDTO;
import com.ecommerce.app.dto.UpdateCartItemRequest;
import com.ecommerce.app.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Get current user's cart
     * Accessible by both USER and ADMIN roles
     * Auto-creates cart if user doesn't have one
     *
     * @param authentication the current authentication context
     * @return CartDTO with all cart items and totals
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<CartDTO> getCart(Authentication authentication) {
        String email = getCurrentUserEmail(authentication);
        CartDTO cart = cartService.getCart(email);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add item to cart
     * Accessible by both USER and ADMIN roles
     * - Validates product exists, is active, and has sufficient stock
     * - If product already in cart, increases quantity
     * - If new product, creates new CartItem
     *
     * @param authentication the current authentication context
     * @param request AddToCartRequest with productId and quantity
     * @return updated CartDTO
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            Authentication authentication,
            @RequestBody AddToCartRequest request) {
        String email = getCurrentUserEmail(authentication);
        CartDTO cart = cartService.addToCart(email, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Update cart item quantity
     * Accessible by both USER and ADMIN roles
     * - Verifies cart item ownership before updating
     * - Checks stock availability before update
     * - Updates cart timestamp after modification
     *
     * @param authentication the current authentication context
     * @param itemId the cart item ID to update
     * @param request UpdateCartItemRequest with new quantity
     * @return updated CartDTO
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemRequest request) {
        String email = getCurrentUserEmail(authentication);
        CartDTO cart = cartService.updateCartItem(email, itemId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Helper method to extract email from authentication context
     *
     * @param authentication the current authentication context
     * @return user's email
     */
    private String getCurrentUserEmail(Authentication authentication) {
        return authentication.getName();
    }
}
