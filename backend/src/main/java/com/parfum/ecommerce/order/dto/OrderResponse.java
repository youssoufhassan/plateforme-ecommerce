
package com.parfum.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;

    private List<OrderItemResponse> items;

    public OrderResponse(
            UUID id,
            String status,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            String customerFirstName,
            String customerLastName,
            String customerEmail,
            List<OrderItemResponse> items
    ) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerEmail = customerEmail;
        this.items = items;
    }

    public UUID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
