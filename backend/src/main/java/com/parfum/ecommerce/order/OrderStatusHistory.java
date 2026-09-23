package com.parfum.ecommerce.order;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(nullable = false)
    private String status;

    /** Email de l'administrateur, ou SYSTEM pour un changement automatique. */
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OrderStatusHistory() {}

    public OrderStatusHistory(Order order, String previousStatus, String status, String changedBy, String note) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.status = status;
        this.changedBy = changedBy;
        this.note = note;
    }

    public UUID getId() { return id; }
    public String getPreviousStatus() { return previousStatus; }
    public String getStatus() { return status; }
    public String getChangedBy() { return changedBy; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}