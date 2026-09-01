package com.parfum.ecommerce.identity.dto;

import java.util.UUID;

public class AddressResponse {
    private UUID id;
    private String street;
    private String city;
    private String postalCode;
    private String country;

    public AddressResponse(UUID id, String street, String city, String postalCode, String country) {
        this.id = id;
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