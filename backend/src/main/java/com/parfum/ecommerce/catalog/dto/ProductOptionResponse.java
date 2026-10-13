package com.parfum.ecommerce.catalog.dto;

import java.util.List;
import java.util.UUID;

/** Un axe et ses valeurs, pour construire les sélecteurs côté boutique. */
public record ProductOptionResponse(
        UUID id,
        String name,
        int position,
        List<OptionValue> values
) {
    public record OptionValue(UUID id, String value, int position) {}
}