package com.parfum.ecommerce.supplier.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Un produit trouvé chez un fournisseur, tel qu'il s'affiche dans le back-office
 * avant décision d'import.
 */
public record SupplierCatalogItem(
        String externalId,
        String name,
        String brand,
        String description,
        String imageUrl,
        BigDecimal supplierPrice,
        BigDecimal suggestedPrice,
        String categoryName,
        boolean alreadyImported,
        UUID existingProductId
) {}