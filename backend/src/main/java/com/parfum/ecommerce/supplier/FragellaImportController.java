package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplier/fragella")
public class FragellaImportController {

    private final FragellaImportService fragellaImportService;

    public FragellaImportController(
            FragellaImportService fragellaImportService
    ) {
        this.fragellaImportService = fragellaImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<ImportedProductResponse> importProduct(
            @RequestParam String search
    ) {
        Product product = fragellaImportService.importProduct(search);

        return ResponseEntity.ok(
                new ImportedProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getImageUrl(),
                        product.getCategory() != null
                                ? product.getCategory().getName()
                                : null
                )
        );
    }

    public record ImportedProductResponse(
            java.util.UUID id,
            String name,
            java.math.BigDecimal price,
            Integer stockQuantity,
            String imageUrl,
            String category
    ) {
    }
}