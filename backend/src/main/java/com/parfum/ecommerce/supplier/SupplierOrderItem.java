package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.catalog.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "supplier_order_items")
public class SupplierOrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "supplier_order_id", nullable = false)
    private SupplierOrder supplierOrder;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "supplier_sku")
    private String supplierSku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    public SupplierOrderItem() {}

    public SupplierOrderItem(SupplierOrder supplierOrder, Product product, Integer quantity) {
        this.supplierOrder = supplierOrder;
        this.product = product;
        this.quantity = quantity;
        this.supplierSku = product.getSupplierSku();
        this.unitCost = product.getCostPrice();
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public String getSupplierSku() { return supplierSku; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
}