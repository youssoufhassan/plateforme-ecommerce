package com.parfum.ecommerce.catalog;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Briques de filtrage combinables pour la recherche de produits.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("active"), true);
    }

    public static Specification<Product> matchesText(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + escapeLike(text.trim().toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                    cb.like(cb.lower(root.get("description")), pattern, '\\')
            );
        };
    }

    public static Specification<Product> inCategory(String categoryName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("category").get("name")), categoryName.trim().toLowerCase());
    }

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase());
    }

    public static Specification<Product> priceAtLeast(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceAtMost(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    /**
     * Disponible : au moins une variante active, et soit le produit est en dropshipping,
     * soit la variante a du stock.
     */
    public static Specification<Product> isAvailable() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.or(
                            cb.equal(root.get("fulfillmentType"), "DROPSHIP"),
                            cb.greaterThan(variant.get("stockQuantity"), 0)
                    )
            );
            return cb.exists(sub);
        };
    }

    /** Neutralise les caractères spéciaux de LIKE (% et _) saisis par l'utilisateur. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}