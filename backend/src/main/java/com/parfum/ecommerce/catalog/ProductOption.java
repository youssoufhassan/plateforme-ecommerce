package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Un axe de variation : « Taille », « Couleur », « Contenance ». */
@Entity
@Table(name = "product_options")
public class ProductOption {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position = 0;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, value ASC")
    private List<ProductOptionValue> values = new ArrayList<>();

    public ProductOption() {}

    public ProductOption(Product product, String name, int position) {
        this.product = product;
        this.name = name;
        this.position = position;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public List<ProductOptionValue> getValues() { return values; }
}