package com.parfum.ecommerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddItemRequest {

    @NotNull
    private UUID productId;

    /** Facultatif si le produit n'a qu'une seule variante active. */
    private UUID variantId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}