package com.parfum.ecommerce.cart;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductVariant;
import jakarta.persistence.*;

import java.math.BigDecimal;
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

    /** Prix affiché au client au moment de l'ajout, pour détecter un changement. */
    @Column(name = "price_at_add")
    private BigDecimal priceAtAdd;

    public CartItem() {}

    public CartItem(Cart cart, ProductVariant variant, Integer quantity) {
        this.cart = cart;
        this.variant = variant;
        this.product = variant.getProduct();
        this.quantity = quantity;
        this.priceAtAdd = variant.getPrice();
    }

    public UUID getId() { return id; }
    public Cart getCart() { return cart; }
    public Product getProduct() { return product; }
    public ProductVariant getVariant() { return variant; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPriceAtAdd() { return priceAtAdd; }
    public void setPriceAtAdd(BigDecimal priceAtAdd) { this.priceAtAdd = priceAtAdd; }
}