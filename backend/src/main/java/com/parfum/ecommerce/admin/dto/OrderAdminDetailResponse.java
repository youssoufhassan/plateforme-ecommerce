package com.parfum.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderAdminDetailResponse(
        UUID id,
        String number,
        String status,
        LocalDateTime createdAt,
        Customer customer,
        ShippingAddress shippingAddress,
        List<Item> items,
        Amounts amounts,
        Payment payment,
        Invoice invoice,
        Shipment shipment,
        List<SupplierOrder> supplierOrders,
        Terms terms,
        List<StatusChange> statusHistory,
        boolean hasDropship,
        boolean hasOwnStock
) {
    public record Customer(UUID id, String name, String email, String phone, boolean guest) {}

    public record ShippingAddress(String firstName, String lastName, String street, String complement,
                                   String postalCode, String city, String countryCode, String phone) {}

    public record Item(UUID productId, String productName, String variantLabel, String imageUrl,
                        int quantity, BigDecimal unitPrice, BigDecimal lineTotal,
                        String fulfillmentType, String supplierName, String supplierSku) {}

    public record Amounts(BigDecimal subtotal, BigDecimal shipping, BigDecimal vat,
                           BigDecimal vatRate, BigDecimal total) {}

    public record Payment(String provider, String status, String reference, LocalDateTime paidAt) {}

    public record Invoice(UUID id, String number, LocalDateTime issuedAt) {}

    public record Shipment(String carrier, String trackingNumber, LocalDateTime shippedAt) {}

    public record SupplierOrder(UUID id, String supplierName, String status,
                                 String externalReference, BigDecimal totalCost) {}

    public record Terms(LocalDateTime acceptedAt, Integer version) {}

    public record StatusChange(String previousStatus, String status, String changedBy,
                                String note, LocalDateTime createdAt) {}
}