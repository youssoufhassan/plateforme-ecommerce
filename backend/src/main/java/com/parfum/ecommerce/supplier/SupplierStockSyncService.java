package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.ProductVariant;
import com.parfum.ecommerce.catalog.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Met à jour la disponibilité et le coût d'achat des produits dropship.
 * Le prix de vente n'est JAMAIS modifié automatiquement : c'est une décision commerciale.
 */
@Service
public class SupplierStockSyncService {

    private static final int BATCH_SIZE = 50;

    private final SupplierAdapterRegistry registry;
    private final ProductVariantRepository variantRepository;
    private final SupplierSyncLogRepository syncLogRepository;

    public SupplierStockSyncService(SupplierAdapterRegistry registry,
                                     ProductVariantRepository variantRepository,
                                     SupplierSyncLogRepository syncLogRepository) {
        this.registry = registry;
        this.variantRepository = variantRepository;
        this.syncLogRepository = syncLogRepository;
    }

    public Map<String, Object> sync(String supplierKey) {
        SupplierSyncLog log = syncLogRepository.save(new SupplierSyncLog(supplierKey, "STOCK_SYNC"));

        try {
            SupplierAdapter adapter = registry.get(supplierKey);

            if (!adapter.capabilities().contains(SupplierAdapter.Capability.STOCK_SYNC)) {
                throw new SupplierException(SupplierErrorType.NOT_SUPPORTED, adapter.getSupplierName(),
                        adapter.getSupplierName() + " ne prend pas en charge la synchronisation du stock.");
            }

            Map<String, ProductVariant> bySku = new LinkedHashMap<>();
            for (ProductVariant v : variantRepository.findByProduct_Supplier_Name(adapter.getSupplierName())) {
                String sku = v.getSupplierSku() != null ? v.getSupplierSku() : v.getProduct().getSupplierSku();
                if (sku != null) bySku.put(sku, v);
            }

            int updated = 0;
            int missing = 0;
            int negativeMargin = 0;

            List<String> skus = new ArrayList<>(bySku.keySet());
            for (int i = 0; i < skus.size(); i += BATCH_SIZE) {
                List<String> batch = skus.subList(i, Math.min(i + BATCH_SIZE, skus.size()));

                Map<String, SupplierStock> stocks;
                try {
                    stocks = adapter.fetchStock(batch);
                } catch (Exception e) {
                    throw SupplierErrors.translate(adapter.getSupplierName(), e);
                }

                for (String sku : batch) {
                    ProductVariant variant = bySku.get(sku);
                    SupplierStock stock = stocks.get(sku);

                    if (stock == null) {
                        // Référence retirée chez le fournisseur : on cesse de la vendre
                        variant.setActive(false);
                        missing++;
                    } else {
                        variant.setActive(stock.available());
                        if (stock.costPrice() != null) {
                            variant.setCostPrice(stock.costPrice());
                        }
                        updated++;
                    }

                    if (variant.getCostPrice() != null
                            && variant.getCostPrice().compareTo(variant.getPrice()) >= 0) {
                        negativeMargin++;
                    }

                    variantRepository.save(variant);
                }
            }

            String message = updated + " variante(s) mise(s) à jour, " + missing
                    + " retirée(s) du catalogue fournisseur"
                    + (negativeMargin > 0 ? ", ATTENTION : " + negativeMargin + " vendue(s) à perte" : "");

            log.succeed(updated, missing, message);
            syncLogRepository.save(log);

            return Map.of("updated", updated, "missing", missing,
                    "negativeMargin", negativeMargin, "message", message);

        } catch (SupplierException e) {
            log.fail(e.getType(), e.getMessage());
            syncLogRepository.save(log);
            throw e;
        }
    }
}