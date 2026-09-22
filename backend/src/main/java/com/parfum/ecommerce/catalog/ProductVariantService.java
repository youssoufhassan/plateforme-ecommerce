package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.VariantAdminRequest;
import com.parfum.ecommerce.catalog.dto.VariantAdminResponse;
import com.parfum.ecommerce.order.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductVariantService(ProductRepository productRepository,
                                  ProductVariantRepository variantRepository,
                                  OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<VariantAdminResponse> list(UUID productId) {
        findProduct(productId);
        return variantRepository.findByProductIdOrderByPositionAsc(productId)
                .stream()
                .map(ProductVariantService::toResponse)
                .toList();
    }

    @Transactional
    public VariantAdminResponse create(UUID productId, VariantAdminRequest request) {
        Product product = findProduct(productId);

        String sku = blankToNull(request.getSku());
        if (sku != null && variantRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Cette référence (SKU) est déjà utilisée : " + sku);
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        apply(variant, request, sku);

        product.getVariants().add(variant);
        variantRepository.save(variant);
        syncProductFields(product);

        return toResponse(variant);
    }

    @Transactional
    public VariantAdminResponse update(UUID variantId, VariantAdminRequest request) {
        ProductVariant variant = findVariant(variantId);

        String sku = blankToNull(request.getSku());
        if (sku != null && variantRepository.existsBySkuAndIdNot(sku, variantId)) {
            throw new IllegalArgumentException("Cette référence (SKU) est déjà utilisée : " + sku);
        }

        apply(variant, request, sku);
        variantRepository.save(variant);
        syncProductFields(variant.getProduct());

        return toResponse(variant);
    }

    /**
     * Supprime une variante jamais commandée.
     * Une variante présente dans une commande est seulement désactivée :
     * l'historique des commandes et des factures doit rester intact.
     */
    @Transactional
    public Map<String, String> delete(UUID variantId) {
        ProductVariant variant = findVariant(variantId);
        Product product = variant.getProduct();

        if (orderItemRepository.existsByVariantId(variantId)) {
            variant.setActive(false);
            variantRepository.save(variant);
            syncProductFields(product);
            return Map.of("result", "DEACTIVATED",
                    "message", "Variante déjà commandée : elle a été désactivée au lieu d'être supprimée");
        }

        product.getVariants().remove(variant);
        variantRepository.delete(variant);
        syncProductFields(product);
        return Map.of("result", "DELETED", "message", "Variante supprimée");
    }

    /**
     * Garde les anciennes colonnes du produit cohérentes avec les variantes,
     * tant que le back-office les utilise encore.
     */
    @Transactional
    public void syncProductFields(Product product) {
        List<ProductVariant> active = product.getActiveVariants();

        active.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .ifPresent(product::setPrice);

        product.setStockQuantity(active.stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum());

        productRepository.save(product);
    }

    private void apply(ProductVariant variant, VariantAdminRequest request, String sku) {
        variant.setLabel(request.getLabel().trim());
        variant.setSku(sku);
        variant.setSupplierSku(blankToNull(request.getSupplierSku()));
        variant.setPrice(request.getPrice());
        variant.setCostPrice(request.getCostPrice());
        variant.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        variant.setActive(request.getActive() == null || request.getActive());
        variant.setPosition(request.getPosition() != null ? request.getPosition() : 0);
    }

    public static VariantAdminResponse toResponse(ProductVariant v) {
        BigDecimal margin = null;
        if (v.getCostPrice() != null && v.getCostPrice().signum() > 0) {
            margin = v.getPrice().subtract(v.getCostPrice())
                    .divide(v.getCostPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new VariantAdminResponse(
                v.getId(), v.getLabel(), v.getSku(), v.getSupplierSku(),
                v.getPrice(), v.getCostPrice(), margin,
                v.getStockQuantity(), v.isActive(), v.getPosition()
        );
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
    }

    private ProductVariant findVariant(UUID variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}