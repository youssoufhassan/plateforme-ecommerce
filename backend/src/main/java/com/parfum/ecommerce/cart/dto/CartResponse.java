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

    public CartResponse(List<CartItemResponse> items, BigDecimal subtotal, BigDecimal shipping,
                         BigDecimal vat, BigDecimal vatRate, BigDecimal total,
                         BigDecimal freeShippingThreshold, BigDecimal amountUntilFreeShipping) {
        this.items = items;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.vat = vat;
        this.vatRate = vatRate;
        this.total = total;
        this.freeShippingThreshold = freeShippingThreshold;
        this.amountUntilFreeShipping = amountUntilFreeShipping;
    }

    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShipping() { return shipping; }
    public BigDecimal getVat() { return vat; }
    public BigDecimal getVatRate() { return vatRate; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getFreeShippingThreshold() { return freeShippingThreshold; }
    public BigDecimal getAmountUntilFreeShipping() { return amountUntilFreeShipping; }
}