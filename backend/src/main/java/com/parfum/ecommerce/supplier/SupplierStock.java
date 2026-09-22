package com.parfum.ecommerce.supplier;

import java.math.BigDecimal;

/** Disponibilité et coût d'une référence chez un fournisseur. */
public record SupplierStock(String sku, boolean available, Integer quantity, BigDecimal costPrice) {}