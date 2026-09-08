package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory() != null
                        ? product.getCategory().getName()
                        : null,
                product.getImages()
                        .stream()
                        .map(ProductImage::getUrl)
                        .toList()
        );
    }
}