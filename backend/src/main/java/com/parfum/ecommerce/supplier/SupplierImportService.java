package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Category;
import com.parfum.ecommerce.catalog.CategoryRepository;
import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SupplierImportService {

    private final SupplierAdapterRegistry registry;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SupplierImportService(SupplierAdapterRegistry registry,
                                  SupplierRepository supplierRepository,
                                  ProductRepository productRepository,
                                  CategoryRepository categoryRepository) {
        this.registry = registry;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ImportResult importFrom(String supplierKey, String query, int limit) {
        SupplierAdapter adapter = registry.get(supplierKey);

        Supplier supplier = supplierRepository.findByName(adapter.getSupplierName())
                .orElseGet(() -> supplierRepository.save(
                        new Supplier(adapter.getSupplierName(), "EXTERNAL", null)));

        List<ExternalProduct> externalProducts = adapter.fetchProducts(query, limit);

        int imported = 0;
        int skipped = 0;

        for (ExternalProduct ext : externalProducts) {
            if (ext.getName() == null || ext.getPrice() == null) {
                skipped++;
                continue;
            }
            if (productRepository.existsBySupplierSku(ext.getExternalId())) {
                skipped++;
                continue;
            }

            productRepository.save(toProduct(ext, supplier));
            imported++;
        }

        return new ImportResult(adapter.getSupplierName(), imported, skipped);
    }

    /** Seul endroit où un produit externe devient un Product SHAHIN. */
    private Product toProduct(ExternalProduct ext, Supplier supplier) {
        Product product = new Product();

        product.setName(ext.getName());
        product.setBrand(ext.getBrand());
        product.setDescription(
            ext.getDescription() != null && !ext.getDescription().isBlank()
                ? ext.getDescription()
                : "Description à compléter."
        );
        product.setPrice(ext.getPrice().max(BigDecimal.ONE));
        product.setCostPrice(ext.getCostPrice());
        product.setStockQuantity(0);
        product.setActive(true);
        product.setImageUrl(ext.getImageUrl());
        product.setCategory(resolveCategory(ext.getCategoryName()));

        product.setFulfillmentType("DROPSHIP");
        product.setSupplier(supplier);
        product.setSupplierSku(ext.getExternalId());
        com.parfum.ecommerce.catalog.ProductVariant variant = new com.parfum.ecommerce.catalog.ProductVariant();
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

    public List<Map<String, Object>> listSuppliers() {
        return registry.listAvailable();
    }

    public record ImportResult(String supplier, int imported, int skipped) {}
}