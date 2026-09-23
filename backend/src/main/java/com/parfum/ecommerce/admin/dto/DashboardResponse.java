package com.parfum.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardResponse {

    /** Chiffres du jour et actions en attente. */
    public record Today(
            BigDecimal revenue,
            long orders,
            long ordersToValidate,
            long ordersToShip,
            long supplierOrdersToSend,
            long lowStockAlerts,
            long outOfStock
    ) {}

    /** Performance sur une période, comparée à la précédente. */
    public record Performance(
            String period,
            BigDecimal revenue,
            BigDecimal revenuePreviousPeriod,
            Double revenueChangePercent,
            long orders,
            long ordersPreviousPeriod,
            BigDecimal averageBasket,
            long newCustomers,
            long abandonedOrders
    ) {}

    /** Un point de la courbe d'évolution. */
    public record DailyPoint(LocalDate date, BigDecimal revenue, long orders) {}

    /** Marges : nulles tant que les coûts d'achat ne sont pas saisis. */
    public record Margins(
            BigDecimal estimatedMargin,
            Double marginPercent,
            long variantsWithoutCostPrice,
            boolean reliable
    ) {}

    private Today today;
    private Performance performance;
    private List<DailyPoint> dailyRevenue;
    private Margins margins;

    public DashboardResponse(Today today, Performance performance,
                              List<DailyPoint> dailyRevenue, Margins margins) {
        this.today = today;
        this.performance = performance;
        this.dailyRevenue = dailyRevenue;
        this.margins = margins;
    }

    public Today getToday() { return today; }
    public Performance getPerformance() { return performance; }
    public List<DailyPoint> getDailyRevenue() { return dailyRevenue; }
    public Margins getMargins() { return margins; }
}