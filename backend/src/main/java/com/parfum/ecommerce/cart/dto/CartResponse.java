package com.parfum.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal total;

    public CartResponse(List<CartItemResponse> items, BigDecimal total) {
        this.items = items;
        this.total = total;
    }

    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
}