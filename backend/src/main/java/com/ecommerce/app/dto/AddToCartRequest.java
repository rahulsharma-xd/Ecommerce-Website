package com.ecommerce.app.dto;

public class AddToCartRequest {

    private Long productId;
    private Integer quantity;

    // Constructors
    public AddToCartRequest() {
    }

    public AddToCartRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
