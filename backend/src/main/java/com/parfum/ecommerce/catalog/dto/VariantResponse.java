package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Une variante telle que la voit la boutique.
 * Le stock exact n'est jamais exposé : seule la disponibilité compte.
 */
public record VariantResponse(
        UUID id,
        String label,
        BigDecimal price,
        boolean available,
        /** Valeurs d'axes composant cette variante, pour le sélecteur. */
        List<UUID> optionValueIds
) {}