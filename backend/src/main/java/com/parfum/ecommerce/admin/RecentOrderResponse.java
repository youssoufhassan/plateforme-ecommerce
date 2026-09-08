package com.parfum.ecommerce.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RecentOrderResponse {

    private UUID id;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public RecentOrderResponse(
            UUID id,
            String customerName,
            String customerEmail,
            BigDecimal totalAmount,
            String status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}