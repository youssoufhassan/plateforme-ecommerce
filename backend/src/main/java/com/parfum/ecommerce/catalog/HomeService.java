package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.order.OrderItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HomeService {

    private static final int BESTSELLER_WINDOW_DAYS = 90;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public HomeService(ProductRepository productRepository,
                        CategoryRepository categoryRepository,
                        OrderItemRepository orderItemRepository,
                        ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> homepage(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 12);

        Map<String, Object> home = new LinkedHashMap<>();
        home.put("featured", featured(safeLimit));
        home.put("newest", newest(safeLimit));
        home.put("bestSellers", bestSellers(safeLimit));
        home.put("categories", categoryRepository.findAll().stream()
                .map(c -> Map.of("id", c.getId(), "name", c.getName()))
                .toList());
        return home;
    }

    /** Produits choisis par l'administrateur. À défaut, les nouveautés. */
    @Transactional(readOnly = true)
    public List<ProductResponse> featured(int limit) {
        List<ProductResponse> result = productRepository.findAll(
                        available().and(ProductSpecifications.isFeatured()),
                        PageRequest.of(0, limit, byNewest()))
                .stream()
                .map(productService::toResponse)
                .toList();

        return result.isEmpty() ? newest(limit) : result;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> newest(int limit) {
        return productRepository.findAll(available(), PageRequest.of(0, limit, byNewest()))
                .stream()
                .map(productService::toResponse)
                .toList();
    }

    /**
     * Meilleures ventes des 90 derniers jours.
     * Liste vide tant qu'aucune commande n'a été payée : le frontend masque alors la section.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> bestSellers(int limit) {
        List<UUID> ids = orderItemRepository.findBestSellerIds(
                LocalDateTime.now().minusDays(BESTSELLER_WINDOW_DAYS),
                PageRequest.of(0, limit));

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findAll(
                available().and(ProductSpecifications.idIn(ids)));

        // Rétablit l'ordre de vente, que la requête de chargement ne conserve pas
        return products.stream()
                .sorted(Comparator.comparingInt(p -> ids.indexOf(p.getId())))
                .map(productService::toResponse)
                .toList();
    }

    private Specification<Product> available() {
        return ProductSpecifications.isActive().and(ProductSpecifications.isAvailable());
    }

    private Sort byNewest() {
        return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
    }
}