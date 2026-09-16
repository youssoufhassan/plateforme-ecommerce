package com.parfum.ecommerce.supplier;

import java.util.List;

/**
 * Contrat que tout fournisseur doit respecter.
 * Ajouter un fournisseur = créer une classe implémentant cette interface.
 * Aucun autre fichier du projet n'a besoin d'être modifié.
 */
public interface SupplierAdapter {

    String getSupplierKey();

    String getSupplierName();

    /**
     * Récupère des produits du fournisseur, traduits en modèle pivot.
     * @param query terme de recherche (certains fournisseurs n'exposent pas de liste complète)
     * @param limit nombre maximum de produits à ramener
     */
    List<ExternalProduct> fetchProducts(String query, int limit);

    /** Faux si le fournisseur n'est pas configuré (clé API absente par exemple). */
    default boolean isAvailable() {
        return true;
    }
}