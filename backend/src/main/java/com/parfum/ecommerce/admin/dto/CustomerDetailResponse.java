package com.parfum.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerDetailResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean guest,
        boolean emailVerified,
        boolean marketingConsent,
        LocalDateTime createdAt,
        Stats stats,
        List<Address> addresses,
        List<OrderSummary> orders
) {
    public record Stats(long orderCount, BigDecimal totalSpent,
                         BigDecimal averageBasket, LocalDateTime lastOrderAt) {}

    public record Address(String firstName, String lastName, String street, String complement,
                           String postalCode, String city, String countryCode, String phone) {}

    public record OrderSummary(UUID id, String number, LocalDateTime createdAt,
                                BigDecimal total, String status) {}
}