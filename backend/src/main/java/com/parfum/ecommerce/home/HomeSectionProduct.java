package com.parfum.ecommerce.home;

import com.parfum.ecommerce.catalog.Product;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "home_section_products")
public class HomeSectionProduct {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private HomeSection section;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer position = 0;

    public HomeSectionProduct() {}

    public HomeSectionProduct(HomeSection section, Product product, Integer position) {
        this.section = section;
        this.product = product;
        this.position = position;
    }

    public UUID getId() { return id; }
    public HomeSection getSection() { return section; }
    public Product getProduct() { return product; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}