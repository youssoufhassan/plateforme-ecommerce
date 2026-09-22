package com.parfum.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private boolean available;
    private String imageUrl;
    private List<String> imageUrls;
    private String categoryName;
    private List<VariantResponse> variants;

    public ProductResponse(UUID id, String name, String description, String brand,
                            BigDecimal price, boolean available, String imageUrl,
                            List<String> imageUrls, String categoryName,
                            List<VariantResponse> variants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.available = available;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls;
        this.categoryName = categoryName;
        this.variants = variants;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    /** Prix le plus bas parmi les variantes ("à partir de"). */
    public BigDecimal getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getCategoryName() { return categoryName; }
    public List<VariantResponse> getVariants() { return variants; }
}