package com.parfum.ecommerce.home.dto;

import com.parfum.ecommerce.catalog.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        String slug,
        String title,
        String subtitle,
        int position,
        boolean active,
        int totalCount,
        List<ProductResponse> products
) {}