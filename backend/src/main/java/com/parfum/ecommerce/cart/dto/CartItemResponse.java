package com.parfum.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItemResponse {
    private UUID itemId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;

    public CartItemResponse(UUID itemId, String productName, BigDecimal unitPrice, Integer quantity) {
        this.itemId = itemId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public UUID getItemId() { return itemId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
}