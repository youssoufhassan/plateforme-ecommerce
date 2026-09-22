package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VariantAdminResponse(
        UUID id,
        String label,
        String sku,
        String supplierSku,
        BigDecimal price,
        BigDecimal costPrice,
        BigDecimal marginPercent,
        Integer stockQuantity,
        boolean active,
        Integer position
) {}