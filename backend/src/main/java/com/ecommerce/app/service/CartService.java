package com.ecommerce.app.service;

import com.ecommerce.app.dto.AddToCartRequest;
import com.ecommerce.app.dto.CartDTO;
import com.ecommerce.app.dto.CartItemDTO;
import com.ecommerce.app.dto.UpdateCartItemRequest;
import com.ecommerce.app.entity.Cart;
import com.ecommerce.app.entity.CartItem;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get cart for current user
     * Auto-creates cart if user doesn't have one
     *
     * @param email the user's email from authentication
     * @return CartDTO with all cart details
     */
    public CartDTO getCart(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Cart cart = getOrCreateCart(user);
        return convertToCartDTO(cart);
    }

    /**
     * Add product to cart with full validation
     * - Validates product exists, is active, and has sufficient stock
     * - If product already in cart, increases quantity
     * - If new product, creates new CartItem
     * - Prevents exceeding available stock
     * - Updates cart timestamp on changes
     *
     * @param email the user's email from authentication
     * @param request the AddToCartRequest with productId and quantity
     * @return updated CartDTO
     */
    public CartDTO addToCart(String email, AddToCartRequest request) {
        // Validate input
        if (request.getProductId() == null) {
            throw new InvalidInputException("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero");
        }

        // Get user and cart
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        Cart cart = getOrCreateCart(user);

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Validate product is active
        if (!product.getActive()) {
            throw new RuntimeException("Product is not available for purchase");
        }

        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
            cart.getId(),
            product.getId()
        );

        if (existingItem.isPresent()) {
            // Update existing cart item
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            // Validate stock availability
            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity()
                    + ", requested: " + newQuantity);
            }

            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            // Validate stock availability
            if (request.getQuantity() > product.getStockQuantity()) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity()
                    + ", requested: " + request.getQuantity());
            }

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtAddition(product.getPrice());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);

            // Add to cart's collection
            cart.getCartItems().add(cartItem);
        }

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return convertToCartDTO(cart);
    }

    /**
     * Update cart item quantity with validation
     * - Verifies cart item ownership (belongs to authenticated user's cart)
     * - Checks stock availability before update
     * - Updates cart timestamp after modification
     *
     * @param email the user's email from authentication
     * @param itemId the cart item ID to update
     * @param request the UpdateCartItemRequest with new quantity
     * @return updated CartDTO
     */
    public CartDTO updateCartItem(String email, Long itemId, UpdateCartItemRequest request) {
        // Validate input
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero");
        }

        // Get user and cart
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        Cart cart = getOrCreateCart(user);

        // Get cart item
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        // Verify ownership - cart item must belong to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to your cart");
        }

        // Get product and check stock availability
        Product product = cartItem.getProduct();
        if (request.getQuantity() > product.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity()
                + ", requested: " + request.getQuantity());
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return convertToCartDTO(cart);
    }

    /**
     * Get existing cart or create new one for user
     * Helper method for internal use
     *
     * @param user the user entity
     * @return Cart entity (existing or newly created)
     */
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCreatedAt(LocalDateTime.now());
                newCart.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(newCart);
            });
    }

    /**
     * Convert Cart entity to CartDTO
     * Calculates total items and total price
     */
    private CartDTO convertToCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getCartItems().stream()
            .map(this::convertToCartItemDTO)
            .collect(Collectors.toList());

        return new CartDTO(cart.getId(), items);
    }

    /**
     * Convert CartItem entity to CartItemDTO
     * Includes product information and subtotal
     */
    private CartItemDTO convertToCartItemDTO(CartItem item) {
        return new CartItemDTO(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getImageUrl(),
            item.getPriceAtAddition(),
            item.getQuantity()
        );
    }
}
