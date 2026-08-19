package com.parfum.ecommerce.payment;

import com.parfum.ecommerce.order.Order;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String provider = "MOCK";

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(Order order, BigDecimal amount) {
        this.order = order;
        this.amount = amount;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public String getProvider() { return provider; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String ref) { this.transactionReference = ref; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}