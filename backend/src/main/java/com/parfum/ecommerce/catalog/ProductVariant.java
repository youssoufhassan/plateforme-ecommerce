package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String label;

    @Column(unique = true)
    private String sku;

    @Column(name = "supplier_sku")
    private String supplierSku;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer position = 0;

    public ProductVariant() {}

    /** Un produit dropshipping est disponible tant que la variante est active. */
    public boolean isAvailable() {
        if (!active) return false;
        if (product != null && "DROPSHIP".equals(product.getFulfillmentType())) return true;
        return stockQuantity != null && stockQuantity > 0;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
        /**
     * Les valeurs qui composent cette variante : une par axe.
     * Un parfum en a une (« 100 ml »), un vêtement deux (« M » et « Noir »).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "variant_option_values",
        joinColumns = @JoinColumn(name = "variant_id"),
        inverseJoinColumns = @JoinColumn(name = "option_value_id"))
    private java.util.Set<ProductOptionValue> optionValues = new java.util.LinkedHashSet<>();

    public java.util.Set<ProductOptionValue> getOptionValues() { return optionValues; }

    public void setOptionValues(java.util.Set<ProductOptionValue> optionValues) {
        this.optionValues = optionValues;
    }

    /** Libellé reconstruit depuis les axes : « M / Noir ». */
    public String buildLabel() {
        if (optionValues.isEmpty()) return label;

        return optionValues.stream()
                .sorted(java.util.Comparator.comparing(v -> v.getOption().getPosition()))
                .map(ProductOptionValue::getValue)
                .reduce((a, b) -> a + " / " + b)
                .orElse(label);
    }
}