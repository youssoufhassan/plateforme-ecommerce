package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String categoryName;
    private java.util.List<String> imageUrls;
    public ProductResponse(UUID id, String name, String description, BigDecimal price,
                            Integer stockQuantity, String imageUrl, String categoryName , java.util.List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
        this.imageUrls = imageUrls;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryName() { return categoryName; }
    public java.util.List<String> getImageUrls() { return imageUrls; }
    
}