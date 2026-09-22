package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.catalog.dto.VariantAdminResponse;
import com.parfum.ecommerce.catalog.dto.VariantResponse;

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
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setLabel("Standard");
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.getVariants().add(variant);
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

        // Compatibilité avec le formulaire admin actuel : un produit à variante unique
        // reçoit directement le prix et le stock saisis
        if (product.getVariants().size() == 1) {
            ProductVariant single = product.getVariants().get(0);
            single.setPrice(request.getPrice());
            if (request.getStockQuantity() != null) {
                single.setStockQuantity(request.getStockQuantity());
            }
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
    List<VariantResponse> variants = product.getActiveVariants().stream()
            .map(v -> new VariantResponse(v.getId(), v.getLabel(), v.getPrice(), v.isAvailable()))
            .toList();

    return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getBrand(),
            product.getLowestPrice(),
            product.isAvailable(),
            product.getImageUrl(),
            product.getImages().stream().map(ProductImage::getUrl).toList(),
            product.getCategory() != null ? product.getCategory().getName() : null,
            variants
    );
}
private ProductAdminResponse toAdminResponse(Product product) {
    List<VariantAdminResponse> variants = product.getVariants().stream()
            .map(ProductVariantService::toResponse)
            .toList();

    // Coût et marge de la variante la moins chère, pour l'affichage en liste
    ProductVariant cheapest = product.getActiveVariants().stream()
            .min(java.util.Comparator.comparing(ProductVariant::getPrice))
            .orElse(null);

    BigDecimal cost = cheapest != null ? cheapest.getCostPrice() : product.getCostPrice();
    BigDecimal margin = cheapest != null ? ProductVariantService.toResponse(cheapest).marginPercent() : null;

    int totalStock = product.getActiveVariants().stream()
            .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
            .sum();

    return new ProductAdminResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getBrand(),
            product.getLowestPrice(),
            cost,
            margin,
            totalStock,
            Boolean.TRUE.equals(product.getActive()),
            product.getFulfillmentType(),
            product.getSupplier() != null ? product.getSupplier().getName() : null,
            product.getSupplierSku(),
            product.getImageUrl(),
            product.getImages().stream().map(ProductImage::getUrl).toList(),
            product.getCategory() != null ? product.getCategory().getName() : null,
            variants
    );
}

public List<ProductAdminResponse> getAllProductsAdmin() {
    return productRepository.findAll().stream().map(this::toAdminResponse).toList();
}
}