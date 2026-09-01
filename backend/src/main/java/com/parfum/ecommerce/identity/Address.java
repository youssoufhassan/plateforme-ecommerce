package com.parfum.ecommerce.identity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    public Address() {}

    public Address(User user, String street, String city, String postalCode, String country) {
        this.user = user;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UUID getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
}