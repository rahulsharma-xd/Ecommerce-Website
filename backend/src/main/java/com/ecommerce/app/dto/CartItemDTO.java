package com.ecommerce.app.dto;

import java.math.BigDecimal;

public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal priceAtAddition;
    private Integer quantity;
    private BigDecimal subtotal;

    public CartItemDTO(Long id, Long productId, String productName, String productImageUrl,
                      BigDecimal priceAtAddition, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.priceAtAddition = priceAtAddition;
        this.quantity = quantity;
        this.subtotal = priceAtAddition.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImageUrl() { return productImageUrl; }
    public BigDecimal getPriceAtAddition() { return priceAtAddition; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
