package com.parfum.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "Le statut est obligatoire")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}