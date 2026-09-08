package com.parfum.ecommerce.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenuePointResponse {

    private LocalDate date;
    private BigDecimal revenue;

    public RevenuePointResponse(LocalDate date, BigDecimal revenue) {
        this.date = date;
        this.revenue = revenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}