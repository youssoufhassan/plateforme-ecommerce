package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSearchService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public ProductSearchService(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, String category, String brand,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 boolean availableOnly, String sort,
                                                 int page, int size) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Le prix minimum ne peut pas dépasser le prix maximum");
        }

        Specification<Product> spec = ProductSpecifications.isActive();

        if (q != null && !q.isBlank()) spec = spec.and(ProductSpecifications.matchesText(q));
        if (category != null && !category.isBlank()) spec = spec.and(ProductSpecifications.inCategory(category));
        if (brand != null && !brand.isBlank()) spec = spec.and(ProductSpecifications.hasBrand(brand));
        if (minPrice != null) spec = spec.and(ProductSpecifications.priceAtLeast(minPrice));
        if (maxPrice != null) spec = spec.and(ProductSpecifications.priceAtMost(maxPrice));
        if (availableOnly) spec = spec.and(ProductSpecifications.isAvailable());

        int safePage = Math.max(0, page);
        int safeSize = (size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        PageRequest pageable = PageRequest.of(safePage, safeSize, toSort(sort));

        return PageResponse.of(productRepository.findAll(spec, pageable), productService::toResponse);
    }

    /** Valeurs disponibles pour construire les filtres côté frontend. */
    @Transactional(readOnly = true)
    public Map<String, Object> filters() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("categories", categoryRepository.findAll().stream().map(Category::getName).sorted().toList());
        filters.put("brands", productRepository.findActiveBrands());
        filters.put("minPrice", productRepository.findMinActivePrice());
        filters.put("maxPrice", productRepository.findMaxActivePrice());
        filters.put("sorts", List.of("relevance", "price_asc", "price_desc", "newest", "name"));
        return filters;
    }

    /**
     * Le tri secondaire par identifiant garantit un ordre stable entre les pages.
     */
    private Sort toSort(String sort) {
        Sort tieBreaker = Sort.by("id");

        if (sort == null) sort = "relevance";

        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(tieBreaker);
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(tieBreaker);
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt").and(tieBreaker);
            case "name" -> Sort.by(Sort.Direction.ASC, "name").and(tieBreaker);
            default -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("name")).and(tieBreaker);
        };
    }
        /**
     * Produits suggérés sur une fiche produit.
     * Priorité : même marque, puis même catégorie à prix proche.
     * Seuls des produits actifs et disponibles sont proposés.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> similar(java.util.UUID productId, int limit) {
        Product current = productRepository.findById(productId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        int safeLimit = Math.min(Math.max(limit, 1), 12);

        Specification<Product> base = ProductSpecifications.isActive()
                .and(ProductSpecifications.isAvailable())
                .and(ProductSpecifications.notId(productId));

        // Ordre stable : nouveautés d'abord, puis identifiant
        Sort order = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));

        java.util.Map<java.util.UUID, Product> found = new LinkedHashMap<>();

        // 1. Même marque
        if (current.getBrand() != null && !current.getBrand().isBlank()) {
            productRepository.findAll(
                    base.and(ProductSpecifications.hasBrand(current.getBrand())),
                    PageRequest.of(0, safeLimit, order)
            ).forEach(p -> found.put(p.getId(), p));
        }

        // 2. Même catégorie, prix proche (±40 %)
        if (found.size() < safeLimit && current.getCategory() != null) {
            Specification<Product> sameCategory = base
                    .and(ProductSpecifications.inCategory(current.getCategory().getName()))
                    .and(ProductSpecifications.priceNear(current.getPrice(), 0.40));

            productRepository.findAll(sameCategory, PageRequest.of(0, safeLimit, order))
                    .forEach(p -> found.putIfAbsent(p.getId(), p));
        }

        // 3. Complément : même catégorie sans contrainte de prix
        if (found.size() < safeLimit && current.getCategory() != null) {
            productRepository.findAll(
                    base.and(ProductSpecifications.inCategory(current.getCategory().getName())),
                    PageRequest.of(0, safeLimit, order)
            ).forEach(p -> found.putIfAbsent(p.getId(), p));
        }

        return found.values().stream()
                .limit(safeLimit)
                .map(productService::toResponse)
                .toList();
    }
}