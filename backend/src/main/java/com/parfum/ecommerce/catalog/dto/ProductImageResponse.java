package com.parfum.ecommerce.catalog.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        String altText,
        int position,
        boolean main
) {}