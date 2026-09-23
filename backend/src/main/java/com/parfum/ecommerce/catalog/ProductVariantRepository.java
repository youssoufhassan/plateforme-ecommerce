package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdOrderByPositionAsc(UUID productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);
    List<ProductVariant> findByProduct_Supplier_Name(String supplierName);

        /** Variantes en stock propre sous le seuil d'alerte. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(v) FROM ProductVariant v
        WHERE v.active = true
          AND v.product.fulfillmentType = 'OWN_STOCK'
          AND v.stockQuantity <= :threshold
    """)
    long countLowStock(@org.springframework.data.repository.query.Param("threshold") int threshold);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(v) FROM ProductVariant v
        WHERE v.active = true
          AND v.product.fulfillmentType = 'OWN_STOCK'
          AND v.stockQuantity = 0
    """)
    long countOutOfStock();

    /** Nombre de variantes sans coût d'achat saisi, pour signaler les marges incomplètes. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(v) FROM ProductVariant v
        WHERE v.active = true AND v.costPrice IS NULL
    """)
    long countWithoutCostPrice();
        /** Variantes en stock propre sous le seuil, les plus critiques d'abord. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT v FROM ProductVariant v
        WHERE v.active = true
          AND v.product.active = true
          AND v.product.fulfillmentType = 'OWN_STOCK'
          AND v.stockQuantity <= :threshold
        ORDER BY v.stockQuantity ASC, v.product.name ASC
    """)
    java.util.List<ProductVariant> findLowStock(
            @org.springframework.data.repository.query.Param("threshold") int threshold,
            org.springframework.data.domain.Pageable pageable);
}