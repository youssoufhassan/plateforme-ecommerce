package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.VariantAdminRequest;
import com.parfum.ecommerce.catalog.dto.VariantAdminResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/admin")
public class ProductVariantAdminController {

    private final ProductVariantService variantService;

    public ProductVariantAdminController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    @GetMapping("/{productId}/variants")
    public List<VariantAdminResponse> list(@PathVariable UUID productId) {
        return variantService.list(productId);
    }

    @PostMapping("/{productId}/variants")
    public VariantAdminResponse create(@PathVariable UUID productId,
                                        @Valid @RequestBody VariantAdminRequest request) {
        return variantService.create(productId, request);
    }

    @PutMapping("/variants/{variantId}")
    public VariantAdminResponse update(@PathVariable UUID variantId,
                                        @Valid @RequestBody VariantAdminRequest request) {
        return variantService.update(variantId, request);
    }

    @DeleteMapping("/variants/{variantId}")
    public Map<String, String> delete(@PathVariable UUID variantId) {
        return variantService.delete(variantId);
    }
}