package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdOrderByPositionAsc(UUID productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);
    List<ProductVariant> findByProduct_Supplier_Name(String supplierName);
}