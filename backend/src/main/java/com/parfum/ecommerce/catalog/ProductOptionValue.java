package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;

import java.util.UUID;

/** Une valeur possible d'un axe : « M », « Noir », « 100 ml ». */
@Entity
@Table(name = "product_option_values")
public class ProductOptionValue {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private Integer position = 0;

    public ProductOptionValue() {}

    public ProductOptionValue(ProductOption option, String value, int position) {
        this.option = option;
        this.value = value;
        this.position = position;
    }

    public UUID getId() { return id; }
    public ProductOption getOption() { return option; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}