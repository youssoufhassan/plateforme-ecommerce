package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import com.parfum.ecommerce.catalog.dto.ProductAdminResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));

            product.setCategory(category);
        }

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    public ProductResponse updateProduct(UUID id, CreateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        if (request.getCategoryId() != null) {

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));

            product.setCategory(category);

        } else {
            product.setCategory(null);
        }

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    public ProductResponse getById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        return toResponse(product);
    }

    public void deleteProduct(UUID id) {

        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Produit introuvable");
        }

        productRepository.deleteById(id);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

   private ProductResponse toResponse(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBrand(),
        product.getPrice(),
        product.isAvailable(),
        product.getImageUrl(),
        product.getImages().stream().map(ProductImage::getUrl).toList(),
        product.getCategory() != null ? product.getCategory().getName() : null
    );
}
private ProductAdminResponse toAdminResponse(Product product) {
    BigDecimal margin = null;
    if (product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
        margin = product.getPrice()
                .subtract(product.getCostPrice())
                .divide(product.getCostPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    return new ProductAdminResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBrand(),
        product.getPrice(),
        product.getCostPrice(),
        margin,
        product.getStockQuantity(),
        Boolean.TRUE.equals(product.getActive()),
        product.getFulfillmentType(),
        product.getSupplier() != null ? product.getSupplier().getName() : null,
        product.getSupplierSku(),
        product.getImageUrl(),
        product.getImages().stream().map(ProductImage::getUrl).toList(),
        product.getCategory() != null ? product.getCategory().getName() : null
    );
}

public List<ProductAdminResponse> getAllProductsAdmin() {
    return productRepository.findAll().stream().map(this::toAdminResponse).toList();
}
}