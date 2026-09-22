package com.parfum.ecommerce.order;

import com.parfum.ecommerce.identity.Address;
import com.parfum.ecommerce.identity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @ManyToOne
@JoinColumn(name = "address_id")
private Address address;

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
    @Column(name = "expires_at")
private LocalDateTime expiresAt;
@Column(name = "subtotal_amount")
private BigDecimal subtotalAmount;

@Column(name = "shipping_amount")
private BigDecimal shippingAmount;

@Column(name = "vat_amount")
private BigDecimal vatAmount;

@Column(name = "vat_rate")
private BigDecimal vatRate;
@Column(name = "terms_accepted_at")
private java.time.LocalDateTime termsAcceptedAt;

@Column(name = "terms_version")
private Integer termsVersion;
public BigDecimal getSubtotalAmount() { return subtotalAmount; }
public void setSubtotalAmount(BigDecimal v) { this.subtotalAmount = v; }
public BigDecimal getShippingAmount() { return shippingAmount; }
public void setShippingAmount(BigDecimal v) { this.shippingAmount = v; }
public BigDecimal getVatAmount() { return vatAmount; }
public void setVatAmount(BigDecimal v) { this.vatAmount = v; }
public BigDecimal getVatRate() { return vatRate; }
public void setVatRate(BigDecimal v) { this.vatRate = v; }

public LocalDateTime getExpiresAt() { return expiresAt; }
public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Order() {}
    public Order(User user, BigDecimal totalAmount) {
        this.user = user;
        this.totalAmount = totalAmount;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public Address getAddress() { return address; }
public void setAddress(Address address) { this.address = address; }
public java.time.LocalDateTime getTermsAcceptedAt() { return termsAcceptedAt; }
public void setTermsAcceptedAt(java.time.LocalDateTime termsAcceptedAt) { this.termsAcceptedAt = termsAcceptedAt; }
public Integer getTermsVersion() { return termsVersion; }
public void setTermsVersion(Integer termsVersion) { this.termsVersion = termsVersion; }
}