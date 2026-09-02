package com.parfum.ecommerce.admin;

import java.math.BigDecimal;

public class DashboardStatsResponse {
    private long totalOrders;
    private long totalCustomers;
    private long totalProducts;
    private BigDecimal totalRevenue;
    private long pendingOrders;

    public DashboardStatsResponse(long totalOrders, long totalCustomers, long totalProducts,
                                   BigDecimal totalRevenue, long pendingOrders) {
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalProducts = totalProducts;
        this.totalRevenue = totalRevenue;
        this.pendingOrders = pendingOrders;
    }

    public long getTotalOrders() { return totalOrders; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getTotalProducts() { return totalProducts; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public long getPendingOrders() { return pendingOrders; }
}