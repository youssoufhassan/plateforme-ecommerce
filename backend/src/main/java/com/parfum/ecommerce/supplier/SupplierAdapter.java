package com.parfum.ecommerce.supplier;

import java.util.List;

/**
 * Contrat que tout fournisseur doit respecter.
 */
public interface SupplierAdapter {

    String getSupplierKey();

    String getSupplierName();

    /**
     * Récupère des produits du fournisseur.
     */
    List<ExternalProduct> fetchProducts(String query, int limit);

    /**
     * Récupère une page de produits.
     *
     * Par défaut, les fournisseurs qui ne supportent pas la pagination
     * utilisent uniquement la première page.
     */
    default List<ExternalProduct> fetchProducts(String query, int page, int limit) {
        if (page != 1) {
            throw new UnsupportedOperationException(
                "Ce fournisseur ne supporte pas encore la pagination."
            );
        }

        return fetchProducts(query, limit);
    }

    /** Faux si le fournisseur n'est pas configuré. */
    default boolean isAvailable() {
        return true;
    }
}