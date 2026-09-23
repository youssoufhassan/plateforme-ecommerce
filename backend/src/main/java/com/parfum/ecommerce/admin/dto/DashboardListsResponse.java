package com.parfum.ecommerce.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardListsResponse {

    /** Une commande récente, en version résumée. */
    public record RecentOrder(
            UUID id,
            String number,
            String customerName,
            String customerEmail,
            BigDecimal total,
            String status,
            LocalDateTime createdAt,
            boolean hasDropship
    ) {}

    /** Un produit du classement des ventes. */
    public record TopProduct(
            UUID productId,
            String name,
            long quantitySold,
            BigDecimal revenue
    ) {}

    /** Une variante en stock bas ou en rupture. */
    public record StockAlert(
            UUID productId,
            UUID variantId,
            String productName,
            String variantLabel,
            int stockQuantity,
            boolean outOfStock
    ) {}

    private List<RecentOrder> recentOrders;
    private List<TopProduct> topProducts;
    private List<StockAlert> stockAlerts;

    public DashboardListsResponse(List<RecentOrder> recentOrders,
                                   List<TopProduct> topProducts,
                                   List<StockAlert> stockAlerts) {
        this.recentOrders = recentOrders;
        this.topProducts = topProducts;
        this.stockAlerts = stockAlerts;
    }

    public List<RecentOrder> getRecentOrders() { return recentOrders; }
    public List<TopProduct> getTopProducts() { return topProducts; }
    public List<StockAlert> getStockAlerts() { return stockAlerts; }
}