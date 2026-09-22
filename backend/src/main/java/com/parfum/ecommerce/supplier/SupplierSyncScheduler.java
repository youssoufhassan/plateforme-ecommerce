package com.parfum.ecommerce.supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SupplierSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupplierSyncScheduler.class);

    private final SupplierAdapterRegistry registry;
    private final SupplierStockSyncService syncService;

    @Value("${app.suppliers.sync.enabled:false}")
    private boolean enabled;

    public SupplierSyncScheduler(SupplierAdapterRegistry registry, SupplierStockSyncService syncService) {
        this.registry = registry;
        this.syncService = syncService;
    }

    /** Chaque nuit à 3h : synchronise les fournisseurs capables de fournir leur stock. */
    @Scheduled(cron = "${app.suppliers.sync.cron:0 0 3 * * *}")
    public void syncAll() {
        if (!enabled) return;

        for (SupplierAdapter adapter : registry.all()) {
            if (!adapter.isAvailable()
                    || !adapter.capabilities().contains(SupplierAdapter.Capability.STOCK_SYNC)) {
                continue;
            }
            try {
                syncService.sync(adapter.getSupplierKey());
            } catch (Exception e) {
                // L'échec d'un fournisseur ne doit pas empêcher la synchronisation des autres
                log.warn("Synchronisation {} en echec : {}", adapter.getSupplierKey(), e.getMessage());
            }
        }
    }
}