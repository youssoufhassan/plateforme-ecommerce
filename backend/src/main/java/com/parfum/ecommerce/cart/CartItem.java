package com.parfum.ecommerce.cart;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductVariant;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    public CartItem() {}

    public CartItem(Cart cart, ProductVariant variant, Integer quantity) {
        this.cart = cart;
        this.variant = variant;
        this.product = variant.getProduct();
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public Cart getCart() { return cart; }
    public Product getProduct() { return product; }
    public ProductVariant getVariant() { return variant; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}