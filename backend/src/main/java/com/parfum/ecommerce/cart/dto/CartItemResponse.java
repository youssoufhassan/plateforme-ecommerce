package com.parfum.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItemResponse {

    private UUID itemId;
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String variantLabel;
    private BigDecimal unitPrice;
    private Integer quantity;

    public CartItemResponse(UUID itemId, UUID productId, UUID variantId, String productName,
                             String variantLabel, BigDecimal unitPrice, Integer quantity) {
        this.itemId = itemId;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantLabel = variantLabel;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public UUID getItemId() { return itemId; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantLabel() { return variantLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
}