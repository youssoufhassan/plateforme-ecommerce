package com.parfum.ecommerce.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddressRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'adresse est obligatoire")
    private String street;

    private String complement;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le code postal est obligatoire")
    private String postalCode;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(min = 2, max = 2, message = "Le code pays doit contenir 2 lettres")
    private String countryCode;

    private String phone;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}