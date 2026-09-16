package com.parfum.ecommerce.supplier;

import java.math.BigDecimal;
import java.util.List;

/**
 * Représentation neutre d'un produit venant de n'importe quel fournisseur.
 * Chaque adaptateur traduit le format de SON fournisseur vers cette classe.
 * Le reste du système ne connaît jamais le format brut d'un fournisseur.
 */
public class ExternalProduct {

    private final String externalId;
    private final String name;
    private final String brand;
    private final String description;
    private final BigDecimal price;
    private final BigDecimal costPrice;
    private final String imageUrl;
    private final List<String> additionalImages;
    private final String categoryName;

    public ExternalProduct(String externalId, String name, String brand, String description,
                            BigDecimal price, BigDecimal costPrice, String imageUrl,
                            List<String> additionalImages, String categoryName) {
        this.externalId = externalId;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.costPrice = costPrice;
        this.imageUrl = imageUrl;
        this.additionalImages = additionalImages != null ? additionalImages : List.of();
        this.categoryName = categoryName;
    }

    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getAdditionalImages() { return additionalImages; }
    public String getCategoryName() { return categoryName; }
}