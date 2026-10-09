package com.ecommerce.app.service;

import com.ecommerce.app.dto.AddToCartRequest;
import com.ecommerce.app.dto.CartDTO;
import com.ecommerce.app.dto.UpdateCartItemRequest;
import com.ecommerce.app.entity.Cart;
import com.ecommerce.app.entity.CartItem;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.entity.Role;
import com.ecommerce.app.entity.User;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartService.
 * Tests cart management business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private AddToCartRequest addToCartRequest;
    private UpdateCartItemRequest updateCartItemRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        // Setup test cart
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setActive(true);

        // Setup test cart item
        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setPriceAtAddition(new BigDecimal("99.99"));
        testCartItem.setCreatedAt(LocalDateTime.now());
        testCartItem.setUpdatedAt(LocalDateTime.now());

        // Setup add to cart request
        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);

        // Setup update cart item request
        updateCartItemRequest = new UpdateCartItemRequest();
        updateCartItemRequest.setQuantity(5);
    }

    @Nested
    @DisplayName("Get or Create Cart Tests")
    class GetOrCreateCartTests {

        @Test
        @DisplayName("Should get existing cart for user")
        void shouldGetExistingCartForUser() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

            // When
            CartDTO cartDTO = cartService.getCart("test@example.com");

            // Then
            assertThat(cartDTO).isNotNull();
            assertThat(cartDTO.getId()).isEqualTo(1L);

            verify(userRepository).findByEmail("test@example.com");
            verify(cartRepository).findByUserId(1L);
            verify(cartRepository, never()).save(any(Cart.class)); // Should not create new cart
        }

        @Test
        @DisplayName("Should create new cart if user has no cart")
        void shouldCreateNewCartIfUserHasNoCart() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When
            CartDTO cartDTO = cartService.getCart("test@example.com");

            // Then
            assertThat(cartDTO).isNotNull();

            verify(userRepository).findByEmail("test@example.com");
            verify(cartRepository).findByUserId(1L);
            verify(cartRepository).save(argThat(cart ->
                cart.getUser().getId().equals(1L) &&
                cart.getCreatedAt() != null &&
                cart.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.getCart("nonexistent@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with email: nonexistent@example.com");

            verify(userRepository).findByEmail("nonexistent@example.com");
            verify(cartRepository, never()).findByUserId(anyLong());
        }
    }

    @Nested
    @DisplayName("Add to Cart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should add new item to cart successfully")
        void shouldAddNewItemToCartSuccessfully() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When
            CartDTO cartDTO = cartService.addToCart("test@example.com", addToCartRequest);

            // Then
            assertThat(cartDTO).isNotNull();

            verify(productRepository).findById(1L);
            verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
            verify(cartItemRepository).save(argThat(item ->
                item.getProduct().getId().equals(1L) &&
                item.getQuantity() == 2 &&
                item.getPriceAtAddition().compareTo(new BigDecimal("99.99")) == 0 &&
                item.getCreatedAt() != null
            ));
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should increase quantity when adding existing product")
        void shouldIncreaseQuantityWhenAddingExistingProduct() {
            // Given
            testCartItem.setQuantity(3); // Already has 3
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(testCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            addToCartRequest.setQuantity(2); // Adding 2 more

            // When
            cartService.addToCart("test@example.com", addToCartRequest);

            // Then
            verify(cartItemRepository).save(argThat(item ->
                item.getQuantity() == 5 && // 3 + 2 = 5
                item.getUpdatedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when adding more than available stock (new item)")
        void shouldThrowExceptionWhenExceedingStockForNewItem() {
            // Given
            testProduct.setStockQuantity(5);
            addToCartRequest.setQuantity(10); // Requesting more than available

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock. Available: 5, requested: 10");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when total quantity exceeds stock (existing item)")
        void shouldThrowExceptionWhenTotalQuantityExceedsStock() {
            // Given
            testProduct.setStockQuantity(10);
            testCartItem.setQuantity(8); // Already has 8

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(testCartItem));

            addToCartRequest.setQuantity(5); // Adding 5 more would exceed stock (8 + 5 = 13 > 10)

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock. Available: 10, requested: 13");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when product is inactive")
        void shouldThrowExceptionWhenProductIsInactive() {
            // Given
            testProduct.setActive(false);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product is not available for purchase");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with id: 1");

            verify(productRepository).findById(1L);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when product ID is null")
        void shouldThrowExceptionWhenProductIdIsNull() {
            // Given
            addToCartRequest.setProductId(null);

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Product ID is required");
        }

        @Test
        @DisplayName("Should throw exception when quantity is null")
        void shouldThrowExceptionWhenQuantityIsNull() {
            // Given
            addToCartRequest.setQuantity(null);

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void shouldThrowExceptionWhenQuantityIsZero() {
            // Given
            addToCartRequest.setQuantity(0);

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            // Given
            addToCartRequest.setQuantity(-5);

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Should update cart timestamp when adding item")
        void shouldUpdateCartTimestampWhenAddingItem() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When
            cartService.addToCart("test@example.com", addToCartRequest);

            // Then
            verify(cartRepository).save(argThat(cart ->
                cart.getUpdatedAt() != null
            ));
        }
    }

    @Nested
    @DisplayName("Update Cart Item Tests")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update cart item quantity successfully")
        void shouldUpdateCartItemQuantitySuccessfully() {
            // Given
            testCart.getCartItems().add(testCartItem);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When
            CartDTO cartDTO = cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest);

            // Then
            assertThat(cartDTO).isNotNull();

            verify(cartItemRepository).findById(1L);
            verify(cartItemRepository).save(argThat(item ->
                item.getQuantity() == 5 &&
                item.getUpdatedAt() != null
            ));
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should throw exception when updating with insufficient stock")
        void shouldThrowExceptionWhenUpdatingWithInsufficientStock() {
            // Given
            testProduct.setStockQuantity(3);
            updateCartItemRequest.setQuantity(10);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock. Available: 3, requested: 10");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when cart item not found")
        void shouldThrowExceptionWhenCartItemNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 999L, updateCartItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart item not found with id: 999");

            verify(cartItemRepository).findById(999L);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should verify cart item ownership before update")
        void shouldVerifyCartItemOwnershipBeforeUpdate() {
            // Given - cart item belongs to different cart
            Cart differentCart = new Cart();
            differentCart.setId(999L);
            testCartItem.setCart(differentCart);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart)); // User's cart is ID 1
            when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem)); // Item belongs to cart 999

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart item does not belong to your cart");

            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw exception when update quantity is null")
        void shouldThrowExceptionWhenUpdateQuantityIsNull() {
            // Given
            updateCartItemRequest.setQuantity(null);

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when update quantity is zero")
        void shouldThrowExceptionWhenUpdateQuantityIsZero() {
            // Given
            updateCartItemRequest.setQuantity(0);

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when update quantity is negative")
        void shouldThrowExceptionWhenUpdateQuantityIsNegative() {
            // Given
            updateCartItemRequest.setQuantity(-3);

            // When/Then
            assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Quantity must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Stock Edge Cases")
    class StockEdgeCases {

        @Test
        @DisplayName("Should allow adding exact available stock quantity")
        void shouldAllowAddingExactAvailableStock() {
            // Given
            testProduct.setStockQuantity(10);
            addToCartRequest.setQuantity(10); // Exact match

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When/Then - should not throw exception
            CartDTO cartDTO = cartService.addToCart("test@example.com", addToCartRequest);
            assertThat(cartDTO).isNotNull();
        }

        @Test
        @DisplayName("Should handle zero stock product appropriately")
        void shouldHandleZeroStockProduct() {
            // Given
            testProduct.setStockQuantity(0);
            addToCartRequest.setQuantity(1);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
            when(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> cartService.addToCart("test@example.com", addToCartRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient stock. Available: 0, requested: 1");
        }

        @Test
        @DisplayName("Should handle boundary case - updating to max stock")
        void shouldHandleBoundaryCaseUpdatingToMaxStock() {
            // Given
            testProduct.setStockQuantity(50);
            updateCartItemRequest.setQuantity(50);

            testCart.getCartItems().add(testCartItem);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
            when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

            // When/Then - should not throw exception
            CartDTO cartDTO = cartService.updateCartItem("test@example.com", 1L, updateCartItemRequest);
            assertThat(cartDTO).isNotNull();
        }
    }
}
