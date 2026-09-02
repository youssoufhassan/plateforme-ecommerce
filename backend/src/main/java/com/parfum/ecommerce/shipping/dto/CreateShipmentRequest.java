package com.parfum.ecommerce.shipping.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShipmentRequest {
    @NotBlank private String carrier;
    @NotBlank private String trackingNumber;

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}