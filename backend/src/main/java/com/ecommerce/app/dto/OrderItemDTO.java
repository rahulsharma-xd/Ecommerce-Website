package com.ecommerce.app.dto;

import java.math.BigDecimal;

/**
 * DTO for OrderItem information in API responses.
 * Contains product snapshot at time of purchase.
 */
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;

    // Constructors
    public OrderItemDTO() {
    }

    public OrderItemDTO(Long id, Long productId, String productName, String productImageUrl,
                       Integer quantity, BigDecimal priceAtPurchase, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
