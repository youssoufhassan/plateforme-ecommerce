package com.parfum.ecommerce.supplier;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/suppliers")
public class SupplierController {

    private final SupplierImportService importService;

    public SupplierController(SupplierImportService importService) {
        this.importService = importService;
    }

    @GetMapping
    public List<Map<String, Object>> listSuppliers() {
        return importService.listSuppliers();
    }

    @PostMapping("/{supplierKey}/import")
    public SupplierImportService.ImportResult importProducts(
            @PathVariable String supplierKey,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return importService.importFrom(supplierKey, query, limit);
    }
}