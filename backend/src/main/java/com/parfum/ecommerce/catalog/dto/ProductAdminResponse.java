package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductAdminResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal marginPercent;
    private Integer stockQuantity;
    private boolean active;
    private boolean featured;
    private String fulfillmentType;
    private String supplierName;
    private String supplierSku;
    private String imageUrl;
    private List<String> imageUrls;
    private String categoryName;
    private List<VariantAdminResponse> variants;

    public ProductAdminResponse(UUID id, String name, String description, String brand,
                                 BigDecimal price, BigDecimal costPrice, BigDecimal marginPercent,
                                 Integer stockQuantity, boolean active, boolean featured,
                                 String fulfillmentType, String supplierName, String supplierSku,
                                 String imageUrl, List<String> imageUrls, String categoryName,
                                 List<VariantAdminResponse> variants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.costPrice = costPrice;
        this.marginPercent = marginPercent;
        this.stockQuantity = stockQuantity;
        this.active = active;
        this.featured = featured;
        this.fulfillmentType = fulfillmentType;
        this.supplierName = supplierName;
        this.supplierSku = supplierSku;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls;
        this.categoryName = categoryName;
        this.variants = variants;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    /** Prix le plus bas parmi les variantes actives. */
    public BigDecimal getPrice() { return price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public BigDecimal getMarginPercent() { return marginPercent; }
    /** Somme des stocks des variantes actives. */
    public Integer getStockQuantity() { return stockQuantity; }
    public boolean isActive() { return active; }
    /** Mis en avant sur la page d'accueil. */
    public boolean isFeatured() { return featured; }
    public String getFulfillmentType() { return fulfillmentType; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierSku() { return supplierSku; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getCategoryName() { return categoryName; }
    public List<VariantAdminResponse> getVariants() { return variants; }
}