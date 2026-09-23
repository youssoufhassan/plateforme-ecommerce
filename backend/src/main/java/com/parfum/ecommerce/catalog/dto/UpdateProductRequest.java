package com.parfum.ecommerce.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mise à jour partielle : seuls les champs fournis sont modifiés.
 * Un champ absent (null) reste inchangé, ce qui évite d'écraser
 * une valeur par erreur en envoyant un formulaire incomplet.
 */
public class UpdateProductRequest {

    @Size(max = 150)
    private String name;

    private String description;

    @Size(max = 100)
    private String brand;

    @DecimalMin(value = "0.01", message = "Le prix doit être positif")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Le coût ne peut pas être négatif")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity;

    /** Nom de la catégorie. Elle est créée si elle n'existe pas. */
    private String categoryName;

    private Boolean active;

    private Boolean featured;

    /** OWN_STOCK ou DROPSHIP. */
    private String fulfillmentType;

    /** Nom du fournisseur, ou chaîne vide pour le retirer. */
    private String supplierName;

    @Size(max = 150)
    private String supplierSku;

    /** Image principale : identifiant d'une image existante du produit. */
    private UUID mainImageId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    public String getFulfillmentType() { return fulfillmentType; }
    public void setFulfillmentType(String fulfillmentType) { this.fulfillmentType = fulfillmentType; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }
    public UUID getMainImageId() { return mainImageId; }
    public void setMainImageId(UUID mainImageId) { this.mainImageId = mainImageId; }
}