package com.ecommerce.app.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * OrderItem entity representing individual items in an order.
 * Stores product details at the time of purchase (price snapshot).
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Constructors
    public OrderItem() {
    }

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculate subtotal when quantity changes
        if (this.priceAtPurchase != null) {
            this.subtotal = this.priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
        // Recalculate subtotal when price changes
        if (this.quantity != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // Helper method to calculate subtotal
    public void calculateSubtotal() {
        if (this.priceAtPurchase != null && this.quantity != null) {
            this.subtotal = this.priceAtPurchase.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
