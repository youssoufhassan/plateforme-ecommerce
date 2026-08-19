package com.parfum.ecommerce.order.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;

    public OrderItemResponse(String productName, Integer quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}