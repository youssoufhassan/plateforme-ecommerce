package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductAdminListItem;
import com.parfum.ecommerce.common.dto.PageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductAdminSearchService {

    private static final int DEFAULT_SIZE = 25;
    private static final int MAX_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final com.parfum.ecommerce.supplier.SupplierRepository supplierRepository;

    @Value("${app.stock.low-threshold}")
    private int lowStockThreshold;

    public ProductAdminSearchService(ProductRepository productRepository,
                                      CategoryRepository categoryRepository,
                                      com.parfum.ecommerce.supplier.SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductAdminListItem> search(String q, String category, String brand,
                                                      Boolean active, String fulfillmentType,
                                                      String supplier, String stockStatus,
                                                      Boolean featured, Boolean missingCostPrice,
                                                      String sort, int page, int size) {

                Specification<Product> spec = Specification.unrestricted();

        if (q != null && !q.isBlank()) spec = spec.and(ProductSpecifications.matchesAdminText(q));
        if (category != null && !category.isBlank()) spec = spec.and(ProductSpecifications.inCategory(category));
        if (brand != null && !brand.isBlank()) spec = spec.and(ProductSpecifications.hasBrand(brand));
        if (active != null) spec = spec.and(ProductSpecifications.hasActiveStatus(active));
        if (fulfillmentType != null && !fulfillmentType.isBlank())
            spec = spec.and(ProductSpecifications.hasFulfillmentType(fulfillmentType.toUpperCase()));
        if (supplier != null && !supplier.isBlank()) spec = spec.and(ProductSpecifications.hasSupplier(supplier));
        if (Boolean.TRUE.equals(featured)) spec = spec.and(ProductSpecifications.isFeaturedOnly());
        if (Boolean.TRUE.equals(missingCostPrice)) spec = spec.and(ProductSpecifications.hasMissingCostPrice());

        if ("OUT_OF_STOCK".equalsIgnoreCase(stockStatus)) {
            spec = spec.and(ProductSpecifications.isOutOfStock());
        } else if ("LOW_STOCK".equalsIgnoreCase(stockStatus)) {
            spec = spec.and(ProductSpecifications.hasLowStock(lowStockThreshold));
        }

        int safePage = Math.max(0, page);
        int safeSize = (size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        return PageResponse.of(
                productRepository.findAll(spec, PageRequest.of(safePage, safeSize, toSort(sort))),
                this::toListItem);
    }

    /** Valeurs pour construire les filtres du back-office. */
    @Transactional(readOnly = true)
    public Map<String, Object> filters() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("categories", categoryRepository.findAll().stream().map(Category::getName).sorted().toList());
        filters.put("brands", productRepository.findActiveBrands());
        filters.put("suppliers", supplierRepository.findAll().stream()
                .map(com.parfum.ecommerce.supplier.Supplier::getName).sorted().toList());
        filters.put("fulfillmentTypes", List.of("OWN_STOCK", "DROPSHIP"));
        filters.put("stockStatuses", List.of("IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"));
        filters.put("sorts", List.of("newest", "name", "price_asc", "price_desc", "stock_asc"));
        filters.put("lowStockThreshold", lowStockThreshold);
        return filters;
    }

    private ProductAdminListItem toListItem(Product product) {
        List<ProductVariant> activeVariants = product.getActiveVariants();

        ProductVariant cheapest = activeVariants.stream()
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElse(null);

        BigDecimal cost = cheapest != null ? cheapest.getCostPrice() : null;
        BigDecimal margin = null;

        if (cheapest != null && cost != null && cost.signum() > 0) {
            margin = cheapest.getPrice().subtract(cost)
                    .divide(cost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        int totalStock = activeVariants.stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();

        return new ProductAdminListItem(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getImageUrl(),
                product.getLowestPrice(),
                cost,
                margin,
                totalStock,
                activeVariants.size(),
                Boolean.TRUE.equals(product.getActive()),
                product.isFeatured(),
                product.getFulfillmentType(),
                product.getSupplier() != null ? product.getSupplier().getName() : null,
                stockStatusOf(product, totalStock),
                product.getCreatedAt()
        );
    }

    private String stockStatusOf(Product product, int totalStock) {
        if (!"OWN_STOCK".equals(product.getFulfillmentType())) {
            return "NOT_APPLICABLE"; // le stock est géré par le fournisseur
        }
        if (totalStock == 0) return "OUT_OF_STOCK";
        if (totalStock <= lowStockThreshold) return "LOW_STOCK";
        return "IN_STOCK";
    }

    private Sort toSort(String sort) {
        Sort tieBreaker = Sort.by("id");

        if (sort == null) sort = "newest";

        return switch (sort) {
            case "name" -> Sort.by(Sort.Direction.ASC, "name").and(tieBreaker);
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(tieBreaker);
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(tieBreaker);
            case "stock_asc" -> Sort.by(Sort.Direction.ASC, "stockQuantity").and(tieBreaker);
            default -> Sort.by(Sort.Direction.DESC, "createdAt").and(tieBreaker);
        };
    }
}