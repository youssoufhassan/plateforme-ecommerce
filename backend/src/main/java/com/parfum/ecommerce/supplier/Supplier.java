package com.parfum.ecommerce.supplier;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    /** INTERNAL (mon propre stock) ou EXTERNAL (fournisseur tiers) */
    @Column(nullable = false)
    private String type;

    @Column(name = "api_url")
    private String apiUrl;

    @Column(name = "contact_email")
    private String contactEmail;

    public Supplier() {}

    public Supplier(String name, String type, String apiUrl) {
        this.name = name;
        this.type = type;
        this.apiUrl = apiUrl;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getApiUrl() { return apiUrl; }
    public String getContactEmail() { return contactEmail; }
}