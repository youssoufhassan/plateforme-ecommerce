package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        java.util.Optional<Product> findBySupplierSku(String supplierSku);
        @Query("""
        SELECT DISTINCT p.brand FROM Product p JOIN p.categories c
        WHERE p.active = true AND p.brand IS NOT NULL
          AND (LOWER(c.path) = :path OR LOWER(c.path) LIKE CONCAT(:path, '/%'))
        ORDER BY p.brand
    """)
    java.util.List<String> findBrandsInBranch(@Param("path") String path);

    @Query("""
        SELECT MIN(p.price) FROM Product p JOIN p.categories c
        WHERE p.active = true
          AND (LOWER(c.path) = :path OR LOWER(c.path) LIKE CONCAT(:path, '/%'))
    """)
    java.math.BigDecimal findMinPriceInBranch(@Param("path") String path);

    @Query("""
        SELECT MAX(p.price) FROM Product p JOIN p.categories c
        WHERE p.active = true
          AND (LOWER(c.path) = :path OR LOWER(c.path) LIKE CONCAT(:path, '/%'))
    """)
    java.math.BigDecimal findMaxPriceInBranch(@Param("path") String path);
        /**
     * Marques classées par nombre de produits.
     * Le seuil écarte les marques à référence unique : un bandeau
     * de maisons perd son sens avec un seul produit par nom.
     */
    @Query("""
        SELECT p.brand FROM Product p
        WHERE p.active = true AND p.brand IS NOT NULL AND p.brand <> ''
        GROUP BY p.brand
        HAVING COUNT(p) >= 2
        ORDER BY COUNT(p) DESC, p.brand ASC
    """)
    java.util.List<String> findTopBrands(org.springframework.data.domain.Pageable pageable);
        @Query("""
        SELECT DISTINCT p.gender FROM Product p
        WHERE p.active = true AND p.gender IS NOT NULL
        ORDER BY p.gender
    """)
    java.util.List<String> findDistinctGenders();
}