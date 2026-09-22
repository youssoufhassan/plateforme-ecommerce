package com.parfum.ecommerce.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VariantAdminRequest {

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100)
    private String label;

    @Size(max = 100)
    private String sku;

    @Size(max = 150)
    private String supplierSku;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être positif")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Le coût ne peut pas être négatif")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity = 0;

    private Boolean active = true;

    private Integer position = 0;

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
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}