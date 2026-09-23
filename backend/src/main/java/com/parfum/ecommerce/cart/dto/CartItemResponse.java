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
    private boolean available;
    private Integer maxQuantity;

    public CartItemResponse(UUID itemId, UUID productId, UUID variantId, String productName,
                             String variantLabel, BigDecimal unitPrice, Integer quantity,
                             boolean available, Integer maxQuantity) {
        this.itemId = itemId;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantLabel = variantLabel;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.available = available;
        this.maxQuantity = maxQuantity;
    }

    public UUID getItemId() { return itemId; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantLabel() { return variantLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    /** L'article est-il toujours achetable ? */
    public boolean isAvailable() { return available; }
    /** Quantité maximale commandable, null si illimitée (dropshipping). */
    public Integer getMaxQuantity() { return maxQuantity; }
}