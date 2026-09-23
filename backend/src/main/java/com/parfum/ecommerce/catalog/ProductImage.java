package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String url;

    @Column(name = "alt_text")
    private String altText;

    private Integer position;

    /** Référence de stockage, pour supprimer le fichier. Null pour les images historiques. */
    @Column(name = "storage_reference")
    private String storageReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductImage() {}

    public ProductImage(Product product, String url, String altText, Integer position, String storageReference) {
        this.product = product;
        this.url = url;
        this.altText = altText;
        this.position = position;
        this.storageReference = storageReference;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public String getStorageReference() { return storageReference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}