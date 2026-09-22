package com.parfum.ecommerce.order.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private String productName;
    private String variantLabel;
    private Integer quantity;
    private BigDecimal unitPrice;

    public OrderItemResponse(String productName, String variantLabel, Integer quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.variantLabel = variantLabel;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() { return productName; }
    public String getVariantLabel() { return variantLabel; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}