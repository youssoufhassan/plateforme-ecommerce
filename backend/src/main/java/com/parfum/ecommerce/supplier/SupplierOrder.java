package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.order.Order;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "supplier_orders")
public class SupplierOrder {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /** TO_SEND, SENT, CONFIRMED, SHIPPED, FAILED */
    @Column(nullable = false)
    private String status = "TO_SEND";

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "supplierOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierOrderItem> items = new ArrayList<>();

    public SupplierOrder() {}

    public SupplierOrder(Order order, Supplier supplier) {
        this.order = order;
        this.supplier = supplier;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Supplier getSupplier() { return supplier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SupplierOrderItem> getItems() { return items; }
}