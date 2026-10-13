package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Un produit tel que le voit la boutique.
 * Ni coût d'achat, ni marge, ni fournisseur : ces données restent en admin.
 */
public record ProductResponse(
        UUID id,
        String name,
        String description,
        String brand,
        /** Prix le plus bas des variantes actives. */
        BigDecimal price,
        /** Au moins une variante achetable. */
        boolean available,
        String imageUrl,
        List<String> imageUrls,
        String categoryName,
        List<VariantResponse> variants,
        /** Axes de variation : vide pour un produit sans déclinaison. */
        List<ProductOptionResponse> options
) {}