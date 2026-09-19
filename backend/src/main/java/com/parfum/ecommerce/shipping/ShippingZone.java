package com.parfum.ecommerce.shipping;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "shipping_zones")
public class ShippingZone {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "flat_rate", nullable = false)
    private BigDecimal flatRate;

    @Column(name = "free_threshold")
    private BigDecimal freeThreshold;

    @Column(name = "vat_rate", nullable = false)
    private BigDecimal vatRate;

    public ShippingZone() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getFlatRate() { return flatRate; }
    public BigDecimal getFreeThreshold() { return freeThreshold; }
    public BigDecimal getVatRate() { return vatRate; }
}