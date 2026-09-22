package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        String label,
        BigDecimal price,
        boolean available
) {}