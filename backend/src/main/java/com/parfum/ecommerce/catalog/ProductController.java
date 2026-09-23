package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductAdminResponse;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final HomeService homeService;

    public ProductController(ProductService productService,
                              ProductSearchService productSearchService,
                              HomeService homeService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.homeService = homeService;
    }

    // ===== Public : routes fixes (doivent rester AVANT /{id}) =====

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllActiveProducts();
    }

    @GetMapping("/search")
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productSearchService.search(q, category, brand, minPrice, maxPrice,
                availableOnly, sort, page, size);
    }

    @GetMapping("/filters")
    public Map<String, Object> filters() {
        return productSearchService.filters();
    }

    @GetMapping("/home")
    public Map<String, Object> homepage(@RequestParam(defaultValue = "8") int limit) {
        return homeService.homepage(limit);
    }

    @GetMapping("/featured")
    public List<ProductResponse> featured(@RequestParam(defaultValue = "8") int limit) {
        return homeService.featured(Math.min(Math.max(limit, 1), 12));
    }

    @GetMapping("/best-sellers")
    public List<ProductResponse> bestSellers(@RequestParam(defaultValue = "8") int limit) {
        return homeService.bestSellers(Math.min(Math.max(limit, 1), 12));
    }

    // ===== Admin =====

    @GetMapping("/admin")
    public List<ProductAdminResponse> getAllProductsAdmin() {
        return productService.getAllProductsAdmin();
    }

    @GetMapping("/admin/{id}")
    public ProductResponse getProductAdmin(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping("/admin")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id,
                                                          @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PutMapping("/admin/{id}/featured")
    public ProductAdminResponse setFeatured(@PathVariable UUID id,
                                             @RequestBody Map<String, Boolean> body) {
        return productService.setFeatured(id, Boolean.TRUE.equals(body.get("featured")));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Public : routes variables (doivent rester EN DERNIER) =====

    @GetMapping("/{id}/similar")
    public List<ProductResponse> similar(@PathVariable UUID id,
                                          @RequestParam(defaultValue = "4") int limit) {
        return productSearchService.similar(id, limit);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable UUID id) {
        return productService.getActiveById(id);
    }
}