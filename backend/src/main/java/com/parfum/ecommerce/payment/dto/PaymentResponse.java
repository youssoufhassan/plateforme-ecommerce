package com.parfum.ecommerce.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private UUID id;
    private String status;
    private BigDecimal amount;
    private String transactionReference;
    private LocalDateTime paidAt;

    public PaymentResponse(UUID id, String status, BigDecimal amount, String transactionReference, LocalDateTime paidAt) {
        this.id = id;
        this.status = status;
        this.amount = amount;
        this.transactionReference = transactionReference;
        this.paidAt = paidAt;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getTransactionReference() { return transactionReference; }
    public LocalDateTime getPaidAt() { return paidAt; }
}