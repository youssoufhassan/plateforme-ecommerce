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
        public static Specification<Product> notId(java.util.UUID id) {
        return (root, query, cb) -> cb.notEqual(root.get("id"), id);
    }

    public static Specification<Product> priceNear(BigDecimal reference, double tolerance) {
        BigDecimal min = reference.multiply(BigDecimal.valueOf(1 - tolerance));
        BigDecimal max = reference.multiply(BigDecimal.valueOf(1 + tolerance));
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }
        public static Specification<Product> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    public static Specification<Product> idIn(java.util.List<java.util.UUID> ids) {
        return (root, query, cb) -> root.get("id").in(ids);
    }
        /** Recherche admin : nom, marque, référence produit ou référence de variante. */
    public static Specification<Product> matchesAdminText(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + escapeLike(text.trim().toLowerCase()) + "%";

            Subquery<Long> variantMatch = query.subquery(Long.class);
            Root<ProductVariant> variant = variantMatch.from(ProductVariant.class);
            variantMatch.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.or(
                            cb.like(cb.lower(variant.get("sku")), pattern, '\\'),
                            cb.like(cb.lower(variant.get("supplierSku")), pattern, '\\')
                    )
            );

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                    cb.like(cb.lower(root.get("supplierSku")), pattern, '\\'),
                    cb.exists(variantMatch)
            );
        };
    }

    public static Specification<Product> hasActiveStatus(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Product> hasFulfillmentType(String type) {
        return (root, query, cb) -> cb.equal(root.get("fulfillmentType"), type);
    }

    public static Specification<Product> hasSupplier(String supplierName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("supplier").get("name")), supplierName.trim().toLowerCase());
    }

    public static Specification<Product> isFeaturedOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    /** Au moins une variante active sous le seuil de stock (produits en stock propre). */
    public static Specification<Product> hasLowStock(int threshold) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.equal(root.get("fulfillmentType"), "OWN_STOCK"),
                    cb.lessThanOrEqualTo(variant.get("stockQuantity"), threshold)
            );
            return cb.exists(sub);
        };
    }

    /** Toutes les variantes actives sont à zéro (produits en stock propre). */
    public static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> {
            Subquery<Long> withStock = query.subquery(Long.class);
            Root<ProductVariant> variant = withStock.from(ProductVariant.class);

            withStock.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.greaterThan(variant.get("stockQuantity"), 0)
            );

            return cb.and(
                    cb.equal(root.get("fulfillmentType"), "OWN_STOCK"),
                    cb.not(cb.exists(withStock))
            );
        };
    }

    /** Variantes sans coût d'achat : marges incalculables. */
    public static Specification<Product> hasMissingCostPrice() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.isNull(variant.get("costPrice"))
            );
            return cb.exists(sub);
        };
    }
}