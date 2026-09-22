package com.parfum.ecommerce.identity.dto;

public class ProfileResponse {

    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private boolean emailVerified;
    private boolean guest;

    public ProfileResponse(String email, String firstName, String lastName,
                            String phone, boolean emailVerified, boolean guest) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.emailVerified = emailVerified;
        this.guest = guest;
    }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isGuest() { return guest; }
}