package com.parfum.ecommerce.identity.dto;

import java.util.UUID;

public class AddressResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String street;
    private String complement;
    private String city;
    private String postalCode;
    private String countryCode;
    private String phone;

    public AddressResponse(UUID id, String firstName, String lastName, String street,
                            String complement, String city, String postalCode,
                            String countryCode, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.complement = complement;
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.phone = phone;
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getStreet() { return street; }
    public String getComplement() { return complement; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getCountryCode() { return countryCode; }
    public String getPhone() { return phone; }
}