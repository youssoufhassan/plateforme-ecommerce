package com.parfum.ecommerce.payment.dto;

public class CheckoutSessionResponse {
    private String sessionId;
    private String checkoutUrl;

    public CheckoutSessionResponse(String sessionId, String checkoutUrl) {
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    public String getSessionId() { return sessionId; }
    public String getCheckoutUrl() { return checkoutUrl; }
}