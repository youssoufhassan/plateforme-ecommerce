package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String url;

    @Column(name = "alt_text")
    private String altText;

    private Integer position;

    public ProductImage() {}

    public UUID getId() { return id; }
    public String getUrl() { return url; }
    public Integer getPosition() { return position; }
}