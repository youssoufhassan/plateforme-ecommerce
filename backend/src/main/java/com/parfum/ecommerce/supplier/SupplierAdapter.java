package com.parfum.ecommerce.supplier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contrat que tout fournisseur doit respecter.
 * Seul l'import de produits est obligatoire ; les autres capacités
 * sont optionnelles et déclarées via capabilities().
 */
public interface SupplierAdapter {

    enum Capability { PRODUCT_IMPORT, STOCK_SYNC, ORDER_SUBMISSION, TRACKING }

    String getSupplierKey();

    String getSupplierName();

    List<ExternalProduct> fetchProducts(String query, int limit);

    default boolean isAvailable() {
        return true;
    }

    default Set<Capability> capabilities() {
        return Set.of(Capability.PRODUCT_IMPORT);
    }

    /** Disponibilité et coût actuels, indexés par référence fournisseur. */
    default Map<String, SupplierStock> fetchStock(List<String> skus) {
        throw notSupported("la synchronisation du stock");
    }

    /** Transmet une commande et renvoie la référence attribuée par le fournisseur. */
    default String submitOrder(SupplierOrder order) {
        throw notSupported("l'envoi automatique de commandes");
    }

    default SupplierTracking fetchTracking(String externalReference) {
        throw notSupported("le suivi des colis");
    }

    private SupplierException notSupported(String feature) {
        return new SupplierException(SupplierErrorType.NOT_SUPPORTED, getSupplierName(),
                getSupplierName() + " ne prend pas en charge " + feature + ".");
    }
}