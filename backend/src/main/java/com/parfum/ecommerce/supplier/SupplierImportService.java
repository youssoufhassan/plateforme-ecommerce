package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Category;
import com.parfum.ecommerce.catalog.CategoryRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.catalog.ProductVariant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SupplierImportService {

    private final SupplierAdapterRegistry registry;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierSyncLogRepository syncLogRepository;

    public SupplierImportService(SupplierAdapterRegistry registry,
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
     * Volontairement sans @Transactional : l'appel au fournisseur peut durer
     * plusieurs secondes, il ne doit pas bloquer une connexion à la base.
     * Le journal est ainsi enregistré même quand l'import échoue.
     */
    public ImportResult importFrom(String supplierKey, String query, int limit) {
        SupplierSyncLog log = syncLogRepository.save(new SupplierSyncLog(supplierKey, "IMPORT"));

        try {
            SupplierAdapter adapter = registry.get(supplierKey);

            List<ExternalProduct> externalProducts;
            try {
                externalProducts = adapter.fetchProducts(query, limit);
            } catch (Exception e) {
                throw SupplierErrors.translate(adapter.getSupplierName(), e);
            }

            Supplier supplier = supplierRepository.findByName(adapter.getSupplierName())
                    .orElseGet(() -> supplierRepository.save(
                            new Supplier(adapter.getSupplierName(), "EXTERNAL", null)));

            int imported = 0;
            int skipped = 0;

            for (ExternalProduct ext : externalProducts) {
                if (ext.getName() == null || ext.getPrice() == null
                        || productRepository.existsBySupplierSku(ext.getExternalId())) {
                    skipped++;
                    continue;
                }

                try {
                    productRepository.save(toProduct(ext, supplier));
                    imported++;
                } catch (DataIntegrityViolationException e) {
                    skipped++; // créé entre-temps par un import concurrent
                }
            }

            log.succeed(imported, skipped,
                    imported + " produit(s) importé(s), " + skipped + " ignoré(s) (déjà présents ou incomplets)");
            syncLogRepository.save(log);

            return new ImportResult(adapter.getSupplierName(), imported, skipped);

        } catch (SupplierException e) {
            log.fail(e.getType(), e.getMessage());
            syncLogRepository.save(log);
            throw e;

        } catch (RuntimeException e) {
            log.fail(null, e.getMessage());
            syncLogRepository.save(log);
            throw e;
        }
    }

    public List<Map<String, Object>> listSuppliers() {
        return registry.listAvailable();
    }

    /** Seul endroit où un produit externe devient un produit SHAHIN. */
    private Product toProduct(ExternalProduct ext, Supplier supplier) {
        Product product = new Product();

        product.setName(ext.getName());
        product.setBrand(ext.getBrand());
        product.setDescription(ext.getDescription() != null && !ext.getDescription().isBlank()
                ? ext.getDescription()
                : "Description à compléter.");
        product.setPrice(ext.getPrice().max(BigDecimal.ONE));
        product.setCostPrice(ext.getCostPrice());
        product.setStockQuantity(0);
        product.setActive(true);
        product.setImageUrl(ext.getImageUrl());
        product.setCategory(resolveCategory(ext.getCategoryName()));
        product.setFulfillmentType("DROPSHIP");
        product.setSupplier(supplier);
        product.setSupplierSku(ext.getExternalId());

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setLabel("Standard");
        variant.setPrice(product.getPrice());
        variant.setCostPrice(ext.getCostPrice());
        variant.setSupplierSku(ext.getExternalId());
        variant.setStockQuantity(0);
        product.getVariants().add(variant);

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

    public record ImportResult(String supplier, int imported, int skipped) {}
}