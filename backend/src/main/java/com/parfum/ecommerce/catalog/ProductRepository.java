package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    List<Product> findByActiveTrue();

    boolean existsBySupplierSku(String supplierSku);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true AND p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findActiveBrands();

    @Query("SELECT MIN(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findMinActivePrice();

    @Query("SELECT MAX(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findMaxActivePrice();
}