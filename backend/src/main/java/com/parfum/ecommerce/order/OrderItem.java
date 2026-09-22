package com.parfum.ecommerce.order;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductVariant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    /** Libellé figé au moment de la commande. */
    @Column(name = "variant_label")
    private String variantLabel;

    @Column(nullable = false)
    private Integer quantity;

    /** Prix figé au moment de la commande. */
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(Order order, ProductVariant variant, Integer quantity) {
        this.order = order;
        this.variant = variant;
        this.product = variant.getProduct();
        this.variantLabel = variant.getLabel();
        this.quantity = quantity;
        this.unitPrice = variant.getPrice();
    }

    /** Nom affiché : "Asad — 100 ml", ou "Asad" pour une variante Standard. */
    public String getDisplayName() {
        if (variantLabel == null || variantLabel.isBlank() || "Standard".equalsIgnoreCase(variantLabel)) {
            return product.getName();
        }
        return product.getName() + " — " + variantLabel;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public ProductVariant getVariant() { return variant; }
    public String getVariantLabel() { return variantLabel; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}