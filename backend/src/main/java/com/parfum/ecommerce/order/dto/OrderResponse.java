package com.parfum.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private String status;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal vatAmount;
    private BigDecimal vatRate;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private List<OrderItemResponse> items;

    public OrderResponse(UUID id, String status,
                          BigDecimal subtotalAmount, BigDecimal shippingAmount,
                          BigDecimal vatAmount, BigDecimal vatRate, BigDecimal totalAmount,
                          LocalDateTime createdAt,
                          String customerFirstName, String customerLastName, String customerEmail,
                          List<OrderItemResponse> items) {
        this.id = id;
        this.status = status;
        this.subtotalAmount = subtotalAmount;
        this.shippingAmount = shippingAmount;
        this.vatAmount = vatAmount;
        this.vatRate = vatRate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerEmail = customerEmail;
        this.items = items;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public BigDecimal getShippingAmount() { return shippingAmount; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getVatRate() { return vatRate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCustomerFirstName() { return customerFirstName; }
    public String getCustomerLastName() { return customerLastName; }
    public String getCustomerEmail() { return customerEmail; }
    public List<OrderItemResponse> getItems() { return items; }
}