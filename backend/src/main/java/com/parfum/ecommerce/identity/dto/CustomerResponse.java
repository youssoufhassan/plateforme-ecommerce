package com.parfum.ecommerce.identity.dto;

import java.util.UUID;

public class CustomerResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;

    public CustomerResponse(UUID id, String email, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}