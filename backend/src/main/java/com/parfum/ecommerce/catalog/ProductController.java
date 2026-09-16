package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.parfum.ecommerce.catalog.dto.ProductAdminResponse;
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllActiveProducts();
    }

    @PostMapping("/admin")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }
    @PutMapping("/admin/{id}")
public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable UUID id,
        @Valid @RequestBody CreateProductRequest request
) {
    return ResponseEntity.ok(productService.updateProduct(id, request));
}

@DeleteMapping("/admin/{id}")
public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
}

@GetMapping("/admin/{id}")
public ProductResponse getProductAdmin(@PathVariable UUID id) {
    return productService.getById(id);
}
@GetMapping("/admin")
public List<ProductAdminResponse> getAllProductsAdmin() {
    return productService.getAllProductsAdmin();
}
}