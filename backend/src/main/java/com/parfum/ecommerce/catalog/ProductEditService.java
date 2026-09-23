package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductAdminResponse;
import com.parfum.ecommerce.catalog.dto.UpdateProductRequest;
import com.parfum.ecommerce.supplier.Supplier;
import com.parfum.ecommerce.supplier.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductEditService {

    private static final List<String> FULFILLMENT_TYPES = List.of("OWN_STOCK", "DROPSHIP");

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductService productService;

    public ProductEditService(ProductRepository productRepository,
                               ProductVariantRepository variantRepository,
                               ProductImageRepository imageRepository,
                               CategoryRepository categoryRepository,
                               SupplierRepository supplierRepository,
                               ProductService productService) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productService = productService;
    }

    /** Modifie uniquement les champs fournis. */
    @Transactional
    public ProductAdminResponse update(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        if (notBlank(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }
        if (request.getBrand() != null) {
            product.setBrand(blankToNull(request.getBrand()));
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
        if (notBlank(request.getCategoryName())) {
            product.setCategory(resolveCategory(request.getCategoryName()));
        }
        if (request.getSupplierSku() != null) {
            product.setSupplierSku(blankToNull(request.getSupplierSku()));
        }

        applyFulfillment(product, request);
        applySupplier(product, request);
        applyMainImage(product, request);
        applyPriceAndStock(product, request);

        productRepository.save(product);

        return productService.adminResponseOf(product);
    }

    /**
     * Saisit le coût d'achat de plusieurs variantes en une fois.
     * Conçu pour combler rapidement les produits importés sans coût.
     */
    @Transactional
    public Map<String, Object> bulkSetCostPrice(List<Map<String, Object>> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Aucune variante fournie");
        }
        if (entries.size() > 200) {
            throw new IllegalArgumentException("Maximum 200 variantes par envoi");
        }

        int updated = 0;
        List<String> skipped = new ArrayList<>();

        for (Map<String, Object> entry : entries) {
            Object rawId = entry.get("variantId");
            Object rawCost = entry.get("costPrice");

            if (rawId == null || rawCost == null) {
                skipped.add("Entrée incomplète");
                continue;
            }

            UUID variantId;
            BigDecimal cost;
            try {
                variantId = UUID.fromString(rawId.toString());
                cost = new BigDecimal(rawCost.toString());
            } catch (IllegalArgumentException e) {
                skipped.add(rawId + " : valeur invalide");
                continue;
            }

            if (cost.signum() < 0) {
                skipped.add(variantId + " : coût négatif");
                continue;
            }

            ProductVariant variant = variantRepository.findById(variantId).orElse(null);
            if (variant == null) {
                skipped.add(variantId + " : variante introuvable");
                continue;
            }

            variant.setCostPrice(cost);
            variantRepository.save(variant);
            updated++;
        }

        return Map.of(
                "updated", updated,
                "skipped", skipped.size(),
                "details", skipped,
                "message", updated + " coût(s) enregistré(s), " + skipped.size() + " ignoré(s)");
    }

    /** Applique une marge à toutes les variantes d'un produit, à partir de leur coût. */
    @Transactional
    public ProductAdminResponse applyMarkup(UUID productId, BigDecimal markupPercent) {
        if (markupPercent == null || markupPercent.signum() < 0) {
            throw new IllegalArgumentException("Marge invalide");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        BigDecimal factor = BigDecimal.ONE.add(
                markupPercent.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));

        int updated = 0;
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getCostPrice() == null || variant.getCostPrice().signum() <= 0) {
                continue; // sans coût d'achat, aucune marge applicable
            }
            variant.setPrice(variant.getCostPrice().multiply(factor)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            variantRepository.save(variant);
            updated++;
        }

        if (updated == 0) {
            throw new IllegalStateException(
                    "Aucune variante n'a de coût d'achat : saisissez-le avant d'appliquer une marge");
        }

        syncProductPrice(product);
        productRepository.save(product);

        return productService.adminResponseOf(product);
    }

    // ===== Détails =====

    private void applyFulfillment(Product product, UpdateProductRequest request) {
        if (!notBlank(request.getFulfillmentType())) return;

        String type = request.getFulfillmentType().toUpperCase();
        if (!FULFILLMENT_TYPES.contains(type)) {
            throw new IllegalArgumentException("Approvisionnement invalide : " + type);
        }
        product.setFulfillmentType(type);
    }

    private void applySupplier(Product product, UpdateProductRequest request) {
        if (request.getSupplierName() == null) return;

        if (request.getSupplierName().isBlank()) {
            product.setSupplier(null);
            return;
        }

        Supplier supplier = supplierRepository.findByName(request.getSupplierName().trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fournisseur introuvable : " + request.getSupplierName()));
        product.setSupplier(supplier);
    }

    private void applyMainImage(Product product, UpdateProductRequest request) {
        if (request.getMainImageId() == null) return;

        ProductImage image = imageRepository.findById(request.getMainImageId())
                .orElseThrow(() -> new IllegalArgumentException("Image introuvable"));

        if (!image.getProduct().getId().equals(product.getId())) {
            throw new IllegalArgumentException("Cette image n'appartient pas à ce produit");
        }
        product.setImageUrl(image.getUrl());
    }

    /**
     * Prix et stock vivent sur les variantes.
     * Pour un produit à variante unique, les modifier ici est un raccourci pratique ;
     * pour un produit à plusieurs variantes, il faut passer par chaque variante.
     */
    private void applyPriceAndStock(Product product, UpdateProductRequest request) {
        boolean changesPrice = request.getPrice() != null
                || request.getCostPrice() != null
                || request.getStockQuantity() != null;

        if (!changesPrice) return;

        List<ProductVariant> variants = product.getVariants();

        if (variants.size() != 1) {
            throw new IllegalStateException(
                    "Ce produit a " + variants.size() + " variantes : modifiez le prix, le coût "
                    + "et le stock variante par variante");
        }

        ProductVariant variant = variants.get(0);

        if (request.getPrice() != null) variant.setPrice(request.getPrice());
        if (request.getCostPrice() != null) variant.setCostPrice(request.getCostPrice());
        if (request.getStockQuantity() != null) variant.setStockQuantity(request.getStockQuantity());

        variantRepository.save(variant);
        syncProductPrice(product);
    }

    /** Garde les colonnes du produit cohérentes avec ses variantes. */
    private void syncProductPrice(Product product) {
        List<ProductVariant> active = product.getActiveVariants();

        active.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .ifPresent(product::setPrice);

        product.setStockQuantity(active.stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum());
    }

    private Category resolveCategory(String name) {
        return categoryRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name.trim());
                    return categoryRepository.save(category);
                });
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
        @Transactional(readOnly = true)
    public List<Map<String, Object>> variantsMissingCost(int limit) {
        return variantRepository.findWithoutCostPrice(
                        org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(v -> Map.<String, Object>of(
                        "variantId", v.getId(),
                        "productId", v.getProduct().getId(),
                        "productName", v.getProduct().getName(),
                        "variantLabel", v.getLabel(),
                        "price", v.getPrice()))
                .toList();
    }
}