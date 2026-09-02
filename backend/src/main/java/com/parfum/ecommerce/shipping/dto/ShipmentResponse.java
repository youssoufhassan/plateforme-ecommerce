package com.parfum.ecommerce.shipping.dto;

import java.time.LocalDateTime;

public class ShipmentResponse {
    private String carrier;
    private String trackingNumber;
    private LocalDateTime shippedAt;

    public ShipmentResponse(String carrier, String trackingNumber, LocalDateTime shippedAt) {
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
    }

    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public LocalDateTime getShippedAt() { return shippedAt; }
}