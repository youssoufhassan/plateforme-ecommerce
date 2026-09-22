package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import com.parfum.ecommerce.supplier.Supplier;
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "image_url")
private String imageUrl;
@Column
private String brand;

@Column(name = "fulfillment_type", nullable = false)
private String fulfillmentType = "OWN_STOCK";

@ManyToOne
@JoinColumn(name = "supplier_id")
private Supplier supplier;

@Column(name = "supplier_sku")
private String supplierSku;

@Column(name = "cost_price")
private java.math.BigDecimal costPrice;
@OneToMany(mappedBy = "product")
@OrderBy("position ASC")
private java.util.List<ProductImage> images = new java.util.ArrayList<>();
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("position ASC")
private java.util.List<ProductVariant> variants = new java.util.ArrayList<>();

    public Product() {}

    // Getters et setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
public java.util.List<ProductImage> getImages() { return images; }
public String getBrand() { return brand; }
public void setBrand(String brand) { this.brand = brand; }

public String getFulfillmentType() { return fulfillmentType; }
public void setFulfillmentType(String fulfillmentType) { this.fulfillmentType = fulfillmentType; }

public Supplier getSupplier() { return supplier; }
public void setSupplier(Supplier supplier) { this.supplier = supplier; }

public String getSupplierSku() { return supplierSku; }
public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }

public java.math.BigDecimal getCostPrice() { return costPrice; }
public void setCostPrice(java.math.BigDecimal costPrice) { this.costPrice = costPrice; }
/** Disponible si au moins une variante active est disponible. */
public boolean isAvailable() {
    if (!Boolean.TRUE.equals(this.active)) return false;

    if (variants.isEmpty()) {
        // Produit sans variante (ne devrait plus arriver après la migration)
        if ("DROPSHIP".equals(this.fulfillmentType)) return true;
        return this.stockQuantity != null && this.stockQuantity > 0;
    }

    return variants.stream().anyMatch(ProductVariant::isAvailable);
}
public java.util.List<ProductVariant> getVariants() { return variants; }

/** Variantes actives, dans l'ordre d'affichage. */
public java.util.List<ProductVariant> getActiveVariants() {
    return variants.stream().filter(ProductVariant::isActive).toList();
}

/** Prix le plus bas parmi les variantes actives ("à partir de"). */
public java.math.BigDecimal getLowestPrice() {
    return getActiveVariants().stream()
            .map(ProductVariant::getPrice)
            .min(java.math.BigDecimal::compareTo)
            .orElse(this.price);
}
}