package com.parfum.ecommerce.catalog;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Critères de recherche réutilisables, côté boutique comme côté admin.
 * Chaque méthode renvoie une condition combinable avec les autres.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {}

    // =========================================================
    // BASE
    // =========================================================

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> hasActiveStatus(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Product> notId(UUID id) {
        return (root, query, cb) -> cb.notEqual(root.get("id"), id);
    }

    public static Specification<Product> isFeaturedOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    // =========================================================
    // DISPONIBILITÉ
    // =========================================================

    /**
     * Au moins une variante active en stock.
     * Un produit en dropshipping est réputé disponible : le stock
     * est tenu par le fournisseur, pas par nous.
     */
    public static Specification<Product> isAvailable() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.greaterThan(variant.get("stockQuantity"), 0));

            return cb.or(
                    cb.notEqual(root.get("fulfillmentType"), "OWN_STOCK"),
                    cb.exists(sub));
        };
    }

    /** Toutes les variantes actives sont à zéro (stock propre uniquement). */
    public static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> {
            Subquery<Long> withStock = query.subquery(Long.class);
            Root<ProductVariant> variant = withStock.from(ProductVariant.class);

            withStock.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.greaterThan(variant.get("stockQuantity"), 0));

            return cb.and(
                    cb.equal(root.get("fulfillmentType"), "OWN_STOCK"),
                    cb.not(cb.exists(withStock)));
        };
    }

    /** Au moins une variante active sous le seuil de stock. */
    public static Specification<Product> hasLowStock(int threshold) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.equal(root.get("fulfillmentType"), "OWN_STOCK"),
                    cb.lessThanOrEqualTo(variant.get("stockQuantity"), threshold));

            return cb.exists(sub);
        };
    }

    // =========================================================
    // RECHERCHE TEXTE
    // =========================================================

    /** Recherche boutique : nom, marque ou description. */
    public static Specification<Product> matchesText(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + escapeLike(text.trim().toLowerCase()) + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                    cb.like(cb.lower(root.get("description")), pattern, '\\'));
        };
    }

    /** Recherche admin : ajoute les références produit et variante. */
    public static Specification<Product> matchesAdminText(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + escapeLike(text.trim().toLowerCase()) + "%";

            Subquery<Long> variantMatch = query.subquery(Long.class);
            Root<ProductVariant> variant = variantMatch.from(ProductVariant.class);

            variantMatch.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.or(
                            cb.like(cb.lower(variant.get("sku")), pattern, '\\'),
                            cb.like(cb.lower(variant.get("supplierSku")), pattern, '\\')));

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("brand")), pattern, '\\'),
                    cb.like(cb.lower(root.get("supplierSku")), pattern, '\\'),
                    cb.exists(variantMatch));
        };
    }

    // =========================================================
    // CATÉGORIE
    // =========================================================

    /**
     * Filtre sur une branche entière : « homme » remonte aussi
     * « homme/parfums » et « homme/parfums/eaux-de-parfum ».
     * Accepte un chemin, un slug ou un nom, pour rester compatible
     * avec les liens déjà en circulation.
     */
    public static Specification<Product> inCategoryBranch(String value) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<Product, Category> category = root.join("categories", JoinType.INNER);
            String needle = value.trim().toLowerCase();

            return cb.or(
                    cb.equal(cb.lower(category.get("path")), needle),
                    cb.like(cb.lower(category.get("path")), escapeLike(needle) + "/%", '\\'),
                    cb.equal(cb.lower(category.get("slug")), needle),
                    cb.equal(cb.lower(category.get("name")), needle));
        };
    }

    // =========================================================
    // ATTRIBUTS
    // =========================================================

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase());
    }

    public static Specification<Product> hasFulfillmentType(String type) {
        return (root, query, cb) -> cb.equal(root.get("fulfillmentType"), type);
    }

    public static Specification<Product> hasSupplier(String supplierName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("supplier").get("name")), supplierName.trim().toLowerCase());
    }

    /** Variantes sans coût d'achat : marges incalculables. */
    public static Specification<Product> hasMissingCostPrice() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);

            sub.select(cb.literal(1L)).where(
                    cb.equal(variant.get("product"), root),
                    cb.isTrue(variant.get("active")),
                    cb.isNull(variant.get("costPrice")));

            return cb.exists(sub);
        };
    }

    // =========================================================
    // PRIX
    // =========================================================

    public static Specification<Product> priceAtLeast(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceAtMost(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    /** Fourchette autour d'un prix, pour les suggestions de produits proches. */
    public static Specification<Product> priceNear(BigDecimal reference, double tolerance) {
        return (root, query, cb) -> {
            if (reference == null) return cb.conjunction();

            BigDecimal factor = BigDecimal.valueOf(tolerance);
            BigDecimal min = reference.multiply(BigDecimal.ONE.subtract(factor));
            BigDecimal max = reference.multiply(BigDecimal.ONE.add(factor));

            return cb.between(root.get("price"), min, max);
        };
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    /**
     * Neutralise les caractères spéciaux d'un LIKE.
     * Sans cela, un client cherchant « 50% » obtiendrait tout le catalogue.
     */
    static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    public static Specification<Product> idIn(List<UUID> ids) {
    return (root, query, cb) -> {
        if (ids == null || ids.isEmpty()) {
            return cb.disjunction();
        }

        return root.get("id").in(ids);
    };
}
/**
 * Filtre par catégorie, compatible avec les appels existants.
 */
public static Specification<Product> inCategory(String value) {
    return inCategoryBranch(value);
}
}