package com.parfum.ecommerce.supplier;

/** Suivi d'une commande transmise à un fournisseur. */
public record SupplierTracking(String carrier, String trackingNumber, String status) {}