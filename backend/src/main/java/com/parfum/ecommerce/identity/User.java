package com.parfum.ecommerce.identity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

     @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "first_name")
private String firstName;

@Column(name = "last_name")
private String lastName;

private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new java.util.HashSet<>();
    // Constructors, getters, and setters
    public User() {
    }
    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Role> getRoles() { return roles; }
public void setRoles(Set<Role> roles) { this.roles = roles; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getFirstName() { return firstName; }
public void setFirstName(String firstName) { this.firstName = firstName; }
public String getLastName() { return lastName; }
public void setLastName(String lastName) { this.lastName = lastName; }
public String getPhone() { return phone; }
public void setPhone(String phone) { this.phone = phone; }
}
