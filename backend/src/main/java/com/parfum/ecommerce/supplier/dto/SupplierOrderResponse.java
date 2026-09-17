package com.parfum.ecommerce.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SupplierOrderResponse {

    private UUID id;
    private UUID customerOrderId;
    private String supplierName;
    private String status;
    private String externalReference;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private BigDecimal totalCost;
    private List<ItemDto> items;

    public SupplierOrderResponse(UUID id, UUID customerOrderId, String supplierName, String status,
                                  String externalReference, LocalDateTime createdAt, LocalDateTime sentAt,
                                  BigDecimal totalCost, List<ItemDto> items) {
        this.id = id;
        this.customerOrderId = customerOrderId;
        this.supplierName = supplierName;
        this.status = status;
        this.externalReference = externalReference;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.totalCost = totalCost;
        this.items = items;
    }

    public UUID getId() { return id; }
    public UUID getCustomerOrderId() { return customerOrderId; }
    public String getSupplierName() { return supplierName; }
    public String getStatus() { return status; }
    public String getExternalReference() { return externalReference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public BigDecimal getTotalCost() { return totalCost; }
    public List<ItemDto> getItems() { return items; }

    public record ItemDto(String productName, String supplierSku, int quantity, BigDecimal unitCost) {}
}