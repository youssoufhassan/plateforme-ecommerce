package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Category;
import com.parfum.ecommerce.catalog.CategoryRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductImage;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.catalog.ProductVariant;
import com.parfum.ecommerce.supplier.dto.ImportSelectionRequest;
import com.parfum.ecommerce.supplier.dto.SupplierCatalogItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierCatalogService {

    private final SupplierAdapterRegistry registry;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierSyncLogRepository syncLogRepository;

    @Value("${app.suppliers.default-markup-percent}")
    private BigDecimal defaultMarkupPercent;

    public SupplierCatalogService(SupplierAdapterRegistry registry,
                                   SupplierRepository supplierRepository,
                                   ProductRepository productRepository,
                                   CategoryRepository categoryRepository,
                                   SupplierSyncLogRepository syncLogRepository) {
        this.registry = registry;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.syncLogRepository = syncLogRepository;
    }

    /**
     * Recherche dans le catalogue d'un fournisseur, sans rien importer.
     * Signale les produits déjà présents dans la boutique.
     */
    @Transactional(readOnly = true)
    public List<SupplierCatalogItem> browse(String supplierKey, String query, int limit) {
        SupplierAdapter adapter = registry.get(supplierKey);

        List<ExternalProduct> results;
        try {
            results = adapter.fetchProducts(query, Math.min(Math.max(limit, 1), 50));
        } catch (Exception e) {
            throw SupplierErrors.translate(adapter.getSupplierName(), e);
        }

        return results.stream().map(this::toCatalogItem).toList();
    }

    /**
     * Importe les produits sélectionnés, avec le prix de vente choisi.
     * Les produits déjà présents sont ignorés.
     */
    @Transactional
    public Map<String, Object> importSelection(String supplierKey, ImportSelectionRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Aucun produit sélectionné");
        }
        if (request.getItems().size() > 50) {
            throw new IllegalArgumentException("Maximum 50 produits par import");
        }

        SupplierAdapter adapter = registry.get(supplierKey);
        SupplierSyncLog log = syncLogRepository.save(new SupplierSyncLog(supplierKey, "IMPORT"));

        try {
            // On récupère les résultats de la recherche pour disposer des informations complètes
            List<ExternalProduct> available;
            try {
                available = adapter.fetchProducts(request.getQuery(), 50);
            } catch (Exception e) {
                throw SupplierErrors.translate(adapter.getSupplierName(), e);
            }

            Map<String, ExternalProduct> byId = new LinkedHashMap<>();
            available.forEach(p -> byId.put(p.getExternalId(), p));

            Supplier supplier = supplierRepository.findByName(adapter.getSupplierName())
                    .orElseGet(() -> supplierRepository.save(
                            new Supplier(adapter.getSupplierName(), "EXTERNAL", null)));

            int imported = 0;
            List<String> skipped = new ArrayList<>();

            for (ImportSelectionRequest.Selection selection : request.getItems()) {
                ExternalProduct external = byId.get(selection.getExternalId());

                if (external == null) {
                    skipped.add(selection.getExternalId() + " : introuvable dans les résultats");
                    continue;
                }
                if (productRepository.existsBySupplierSku(external.getExternalId())) {
                    skipped.add(external.getName() + " : déjà dans la boutique");
                    continue;
                }

                BigDecimal price = selection.getPrice() != null
                        ? selection.getPrice()
                        : suggestedPrice(external.getPrice());

                if (price == null || price.signum() <= 0) {
                    skipped.add(external.getName() + " : prix de vente manquant");
                    continue;
                }

                productRepository.save(buildProduct(external, supplier, price, selection));
                imported++;
            }

            String message = imported + " produit(s) importé(s), " + skipped.size() + " ignoré(s)";
            log.succeed(imported, skipped.size(), message);
            syncLogRepository.save(log);

            return Map.of(
                    "supplier", adapter.getSupplierName(),
                    "imported", imported,
                    "skipped", skipped.size(),
                    "details", skipped,
                    "message", message);

        } catch (SupplierException e) {
            log.fail(e.getType(), e.getMessage());
            syncLogRepository.save(log);
            throw e;
        }
    }

    private SupplierCatalogItem toCatalogItem(ExternalProduct external) {
        Product existing = productRepository.findBySupplierSku(external.getExternalId()).orElse(null);

        return new SupplierCatalogItem(
                external.getExternalId(),
                external.getName(),
                external.getBrand(),
                external.getDescription(),
                external.getImageUrl(),
                external.getPrice(),
                suggestedPrice(external.getPrice()),
                external.getCategoryName(),
                existing != null,
                existing != null ? existing.getId() : null
        );
    }

    /** Prix de vente suggéré : prix fournisseur majoré de la marge configurée. */
    private BigDecimal suggestedPrice(BigDecimal supplierPrice) {
        if (supplierPrice == null) return null;

        BigDecimal markup = defaultMarkupPercent != null ? defaultMarkupPercent : BigDecimal.ZERO;

        return supplierPrice
                .multiply(BigDecimal.ONE.add(markup.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Product buildProduct(ExternalProduct external, Supplier supplier,
                                  BigDecimal price, ImportSelectionRequest.Selection selection) {
        Product product = new Product();

        product.setName(external.getName());
        product.setBrand(external.getBrand());
        product.setDescription(external.getDescription() != null && !external.getDescription().isBlank()
                ? external.getDescription()
                : "Description à compléter.");
        product.setPrice(price);
        product.setCostPrice(external.getPrice());
        product.setStockQuantity(0);
        product.setActive(selection.getActive() == null || selection.getActive());
        product.setImageUrl(external.getImageUrl());
        product.setCategory(resolveCategory(
                selection.getCategoryName() != null ? selection.getCategoryName() : external.getCategoryName()));
        product.setFulfillmentType("DROPSHIP");
        product.setSupplier(supplier);
        product.setSupplierSku(external.getExternalId());

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setLabel("Standard");
        variant.setPrice(price);
        variant.setCostPrice(external.getPrice());
        variant.setSupplierSku(external.getExternalId());
        variant.setStockQuantity(0);
        product.getVariants().add(variant);

        // L'image du fournisseur devient aussi la première image de la galerie
        if (external.getImageUrl() != null && !external.getImageUrl().isBlank()) {
            product.getImages().add(new ProductImage(
                    product, external.getImageUrl(), external.getName(), 0, null));
        }

        return product;
    }

    private Category resolveCategory(String categoryName) {
        String name = (categoryName == null || categoryName.isBlank()) ? "Divers" : categoryName;

        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    return categoryRepository.save(category);
                });
    }
}