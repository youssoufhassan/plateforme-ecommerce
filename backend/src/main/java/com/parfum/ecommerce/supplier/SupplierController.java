package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.common.dto.PageResponse;
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

    public SupplierController(SupplierImportService importService,
                               SupplierStockSyncService stockSyncService,
                               SupplierSyncLogRepository syncLogRepository) {
        this.importService = importService;
        this.stockSyncService = stockSyncService;
        this.syncLogRepository = syncLogRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listSuppliers() {
        return importService.listSuppliers();
    }

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