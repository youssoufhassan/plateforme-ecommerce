package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Version allégée pour la liste admin : pas de description ni de galerie complète,
 * pour garder la page rapide même avec des centaines de produits.
 */
public record ProductAdminListItem(
        UUID id,
        String name,
        String brand,
        String categoryName,
        String imageUrl,
        BigDecimal price,
        BigDecimal costPrice,
        BigDecimal marginPercent,
        Integer stockQuantity,
        int variantCount,
        boolean active,
        boolean featured,
        String fulfillmentType,
        String supplierName,
        String stockStatus,
        LocalDateTime createdAt
) {}