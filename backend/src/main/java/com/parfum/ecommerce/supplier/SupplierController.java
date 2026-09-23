package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.common.dto.PageResponse;
import com.parfum.ecommerce.supplier.dto.ImportSelectionRequest;
import com.parfum.ecommerce.supplier.dto.SupplierCatalogItem;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/suppliers")
public class SupplierController {

    private final SupplierImportService importService;
    private final SupplierStockSyncService stockSyncService;
    private final SupplierSyncLogRepository syncLogRepository;
    private final SupplierCatalogService catalogService;

    public SupplierController(SupplierImportService importService,
                               SupplierStockSyncService stockSyncService,
                               SupplierSyncLogRepository syncLogRepository,
                               SupplierCatalogService catalogService) {
        this.importService = importService;
        this.stockSyncService = stockSyncService;
        this.syncLogRepository = syncLogRepository;
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<Map<String, Object>> listSuppliers() {
        return importService.listSuppliers();
    }

    /** Recherche dans le catalogue d'un fournisseur, sans rien importer. */
    @GetMapping("/{supplierKey}/catalog")
    public List<SupplierCatalogItem> browse(@PathVariable String supplierKey,
                                             @RequestParam String query,
                                             @RequestParam(defaultValue = "20") int limit) {
        return catalogService.browse(supplierKey, query, limit);
    }

    /** Importe les produits sélectionnés, avec le prix de vente choisi. */
    @PostMapping("/{supplierKey}/catalog/import")
    public Map<String, Object> importSelection(@PathVariable String supplierKey,
                                                @RequestBody ImportSelectionRequest request) {
        return catalogService.importSelection(supplierKey, request);
    }

    /** Import direct des résultats d'une recherche, sans sélection. */
    @PostMapping("/{supplierKey}/import")
    public SupplierImportService.ImportResult importProducts(
            @PathVariable String supplierKey,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "20") int limit) {
        return importService.importFrom(supplierKey, query, Math.min(Math.max(limit, 1), 100));
    }

    @PostMapping("/{supplierKey}/sync-stock")
    public Map<String, Object> syncStock(@PathVariable String supplierKey) {
        return stockSyncService.sync(supplierKey);
    }

    @GetMapping("/sync-logs")
    public PageResponse<SupplierSyncLog> syncLogs(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                syncLogRepository.findAllByOrderByStartedAtDesc(
                        PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100))),
                log -> log);
    }
}