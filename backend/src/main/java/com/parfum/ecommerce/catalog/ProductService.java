package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CreateProductRequest;
import com.parfum.ecommerce.catalog.dto.ProductAdminResponse;
import com.parfum.ecommerce.catalog.dto.ProductOptionResponse;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.catalog.dto.VariantAdminResponse;
import com.parfum.ecommerce.catalog.dto.VariantResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    /** Vue boutique d'un produit : aucune donnée commerciale interne. */
    @Transactional(readOnly = true)
    public ProductResponse toResponse(Product product) {
        List<VariantResponse> variants = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .sorted(Comparator.comparing(ProductVariant::getPosition))
                .map(variant -> new VariantResponse(
                        variant.getId(),
                        variant.getLabel(),
                        variant.getPrice(),
                        isVariantAvailable(product, variant),
                        variant.getOptionValues().stream()
                                .map(ProductOptionValue::getId)
                                .toList()))
                .toList();

        List<ProductOptionResponse> options = product.getOptions().stream()
                .sorted(Comparator.comparing(ProductOption::getPosition))
                .map(option -> new ProductOptionResponse(
                        option.getId(),
                        option.getName(),
                        option.getPosition(),
                        option.getValues().stream()
                                .map(value -> new ProductOptionResponse.OptionValue(
                                        value.getId(), value.getValue(), value.getPosition()))
                                .toList()))
                .toList();

        List<String> imageUrls = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition))
                .map(ProductImage::getUrl)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getLowestPrice(),
                product.isAvailable(),
                product.getImageUrl(),
                imageUrls,
                product.getCategory() != null ? product.getCategory().getName() : null,
                variants,
                options);
    }

    /**
     * Une variante en dropshipping est réputée disponible : le stock
     * est tenu par le fournisseur, pas par nous.
     */
    private boolean isVariantAvailable(Product product, ProductVariant variant) {
        if (!"OWN_STOCK".equals(product.getFulfillmentType())) {
            return true;
        }
        return variant.getStockQuantity() != null && variant.getStockQuantity() > 0;
    }

    private ProductAdminResponse toAdminResponse(Product product) {
        List<VariantAdminResponse> variants = product.getVariants().stream()
                .map(ProductVariantService::toResponse)
                .toList();

        ProductVariant cheapest = product.getActiveVariants().stream()
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElse(null);

        BigDecimal cost = cheapest != null ? cheapest.getCostPrice() : product.getCostPrice();
        BigDecimal margin = cheapest != null
                ? ProductVariantService.toResponse(cheapest).marginPercent()
                : null;

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
                product.isFeatured(),
                product.getFulfillmentType(),
                product.getSupplier() != null ? product.getSupplier().getName() : null,
                product.getSupplierSku(),
                product.getImageUrl(),
                product.getImages().stream().map(ProductImage::getUrl).toList(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                variants
        );
    }

    /** Fiche produit publique : un produit désactivé est introuvable côté boutique. */
    @Transactional(readOnly = true)
    public ProductResponse getActiveById(UUID id) {
        Product product = productRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
        return toResponse(product);
    }

    public List<ProductAdminResponse> getAllProductsAdmin() {
        return productRepository.findAll().stream().map(this::toAdminResponse).toList();
    }

    @Transactional
    public ProductAdminResponse setFeatured(UUID id, boolean featured) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        product.setFeatured(featured);
        productRepository.save(product);

        return toAdminResponse(product);
    }

    /**
     * Applique une action à plusieurs produits d'un coup.
     * Renvoie le nombre de produits réellement modifiés.
     */
    @Transactional
    public Map<String, Object> bulkAction(List<UUID> productIds, String action, String value) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Aucun produit sélectionné");
        }
        if (productIds.size() > 100) {
            throw new IllegalArgumentException("Maximum 100 produits par action groupée");
        }

        List<Product> products = productRepository.findAllById(productIds);
        int updated = 0;

        for (Product product : products) {
            switch (action) {
                case "activate" -> { product.setActive(true); updated++; }
                case "deactivate" -> { product.setActive(false); updated++; }
                case "feature" -> { product.setFeatured(true); updated++; }
                case "unfeature" -> { product.setFeatured(false); updated++; }
                case "category" -> {
                    Category category = categoryRepository.findByName(value)
                            .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable : " + value));
                    product.setCategory(category);
                    updated++;
                }
                default -> throw new IllegalArgumentException("Action inconnue : " + action);
            }
        }

        productRepository.saveAll(products);

        return Map.of(
                "action", action,
                "requested", productIds.size(),
                "updated", updated,
                "notFound", productIds.size() - products.size());
    }

    /** Réponse admin d'un produit, réutilisable par les autres services. */
    @Transactional(readOnly = true)
    public ProductAdminResponse adminResponseOf(Product product) {
        return toAdminResponse(product);
    }
}