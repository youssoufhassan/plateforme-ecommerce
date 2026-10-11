package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  java.util.Optional<Category> findByName(String name);
      java.util.Optional<Category> findByPath(String path);

    boolean existsByPath(String path);

    java.util.List<Category> findByParentIsNullOrderByPositionAscNameAsc();

    /** Tous les descendants d'un nœud, le nœud lui-même exclu. */
    @org.springframework.data.jpa.repository.Query(
        "SELECT c FROM Category c WHERE c.path LIKE CONCAT(:path, '/%') ORDER BY c.path")
    java.util.List<Category> findDescendants(
        @org.springframework.data.repository.query.Param("path") String path);

    /** Produits disponibles dans une branche entière. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(DISTINCT p.id)
        FROM Product p JOIN p.categories c
        WHERE p.active = true
          AND (c.path = :path OR c.path LIKE CONCAT(:path, '/%'))
    """)
    long countProductsInBranch(
        @org.springframework.data.repository.query.Param("path") String path);
}