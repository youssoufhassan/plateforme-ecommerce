package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

   private ProductResponse toResponse(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStockQuantity(),
        product.getImageUrl(),
        product.getCategory() != null ? product.getCategory().getName() : null,
        product.getImages().stream().map(ProductImage::getUrl).toList()
    );
}
}