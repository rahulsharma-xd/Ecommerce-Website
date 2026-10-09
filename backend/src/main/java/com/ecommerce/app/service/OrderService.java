package com.ecommerce.app.service;

import com.ecommerce.app.dto.CheckoutRequest;
import com.ecommerce.app.dto.OrderItemDTO;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.entity.*;
import com.ecommerce.app.exception.InsufficientStockException;
import com.ecommerce.app.exception.InvalidInputException;
import com.ecommerce.app.exception.OrderNotFoundException;
import com.ecommerce.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for order management and checkout processing.
 * Handles order creation, stock deduction, and order retrieval.
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Process checkout with cart items
     * Creates order, deducts stock, clears cart, and sends confirmation email
     *
     * @param email authenticated user's email
     * @param request checkout request with card details
     * @return order response with order details
     */
    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest request) {
        // 1. Validate card details
        paymentService.validateCardDetails(request);

        // 2. Get user
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 3. Get user's cart
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new InvalidInputException("Cart not found for user"));

        // 4. Validate cart is not empty
        if (cart.getCartItems().isEmpty()) {
            throw new InvalidInputException("Cart is empty. Add items before checkout.");
        }

        // 5. Validate stock availability for all items
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getName() + "'. " +
                    "Available: " + product.getStockQuantity() + ", " +
                    "Requested: " + cartItem.getQuantity()
                );
            }
        }

        // 6. Calculate total amount
        BigDecimal totalAmount = cart.getCartItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Process fake payment
        paymentService.processFakePayment(totalAmount, request);

        // 8. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setOrderedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Save order first to get ID
        order = orderRepository.save(order);

        // 9. Create OrderItems and deduct stock
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Create order item with price snapshot
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.calculateSubtotal();

            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);

            // Deduct stock from product
            int newStock = product.getStockQuantity() - cartItem.getQuantity();
            product.setStockQuantity(newStock);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }

        // 10. Clear cart
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // 11. Send confirmation email asynchronously
        emailService.sendOrderConfirmation(user, order);

        // 12. Return order response
        return convertToOrderResponse(order);
    }

    /**
     * Get all orders for authenticated user
     *
     * @param email authenticated user's email
     * @return list of user's orders
     */
    public List<OrderResponse> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return orderRepository.findByUserIdOrderByOrderedAtDesc(user.getId())
            .stream()
            .map(this::convertToOrderResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get specific order by ID with ownership validation
     *
     * @param email authenticated user's email
     * @param orderId order ID
     * @return order response
     */
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Verify ownership
        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderNotFoundException("Order does not belong to you");
        }

        return convertToOrderResponse(order);
    }

    /**
     * Get all orders (admin only)
     *
     * @return list of all orders
     */
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderedAtDesc()
            .stream()
            .map(this::convertToOrderResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get order by ID without ownership check (admin only)
     *
     * @param orderId order ID
     * @return order response
     */
    public OrderResponse getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        return convertToOrderResponse(order);
    }

    /**
     * Update order status (admin only)
     *
     * @param orderId order ID
     * @param newStatus new order status
     * @return updated order response
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    /**
     * Generate unique order number
     * Format: ORD-YYYYMMDD-XXXXX (e.g., ORD-20250127-00001)
     *
     * @return unique order number
     */
    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(ORDER_NUMBER_FORMATTER);
        String prefix = "ORD-" + dateStr + "-";

        // Get count of orders today to generate sequential number
        long count = orderRepository.count() + 1;
        String sequentialNumber = String.format("%05d", count);

        return prefix + sequentialNumber;
    }

    /**
     * Convert Order entity to OrderResponse DTO
     *
     * @param order order entity
     * @return order response DTO
     */
    private OrderResponse convertToOrderResponse(Order order) {
        List<OrderItemDTO> items = order.getOrderItems().stream()
            .map(this::convertToOrderItemDTO)
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getOrderedAt(),
            order.getUpdatedAt(),
            items
        );
    }

    /**
     * Convert OrderItem entity to OrderItemDTO
     *
     * @param item order item entity
     * @return order item DTO
     */
    private OrderItemDTO convertToOrderItemDTO(OrderItem item) {
        return new OrderItemDTO(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getImageUrl(),
            item.getQuantity(),
            item.getPriceAtPurchase(),
            item.getSubtotal()
        );
    }
}
