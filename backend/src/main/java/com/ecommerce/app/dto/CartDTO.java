package com.ecommerce.app.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {
    private Long id;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal totalPrice;

    public CartDTO(Long id, List<CartItemDTO> items) {
        this.id = id;
        this.items = items;
        this.totalItems = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
        this.totalPrice = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters
    public Long getId() { return id; }
    public List<CartItemDTO> getItems() { return items; }
    public Integer getTotalItems() { return totalItems; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
