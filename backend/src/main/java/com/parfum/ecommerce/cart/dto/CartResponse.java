package com.parfum.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal vat;
    private BigDecimal vatRate;
    private BigDecimal total;
    private BigDecimal freeShippingThreshold;
    private BigDecimal amountUntilFreeShipping;
    private boolean checkoutBlocked;
    private List<String> warnings;

    public CartResponse(List<CartItemResponse> items, BigDecimal subtotal, BigDecimal shipping,
                         BigDecimal vat, BigDecimal vatRate, BigDecimal total,
                         BigDecimal freeShippingThreshold, BigDecimal amountUntilFreeShipping,
                         boolean checkoutBlocked, List<String> warnings) {
        this.items = items;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.vat = vat;
        this.vatRate = vatRate;
        this.total = total;
        this.freeShippingThreshold = freeShippingThreshold;
        this.amountUntilFreeShipping = amountUntilFreeShipping;
        this.checkoutBlocked = checkoutBlocked;
        this.warnings = warnings;
    }

    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShipping() { return shipping; }
    public BigDecimal getVat() { return vat; }
    public BigDecimal getVatRate() { return vatRate; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getFreeShippingThreshold() { return freeShippingThreshold; }
    public BigDecimal getAmountUntilFreeShipping() { return amountUntilFreeShipping; }
    /** true si au moins un article empêche la commande. */
    public boolean isCheckoutBlocked() { return checkoutBlocked; }
    /** Messages à afficher au client (indisponibilité, stock réduit, prix modifié). */
    public List<String> getWarnings() { return warnings; }
}