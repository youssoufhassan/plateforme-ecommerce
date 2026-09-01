package com.parfum.ecommerce.identity.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressRequest {
    @NotBlank private String street;
    @NotBlank private String city;
    @NotBlank private String postalCode;
    @NotBlank private String country;

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}