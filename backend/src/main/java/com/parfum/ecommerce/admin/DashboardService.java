package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.admin.dto.DashboardResponse;
import com.parfum.ecommerce.catalog.ProductVariantRepository;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.OrderItemRepository;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.supplier.SupplierOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final SupplierOrderRepository supplierOrderRepository;

    @Value("${app.stock.low-threshold}")
    private int lowStockThreshold;

    public DashboardService(OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             UserRepository userRepository,
                             ProductVariantRepository variantRepository,
                             SupplierOrderRepository supplierOrderRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
        this.supplierOrderRepository = supplierOrderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(String period) {
        return new DashboardResponse(
                today(),
                performance(period),
                dailyRevenue(30),
                margins()
        );
    }

    /** Chiffres du jour et actions en attente. */
    @Transactional(readOnly = true)
    public DashboardResponse.Today today() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return new DashboardResponse.Today(
                scale(orderRepository.revenueBetween(start, end)),
                orderRepository.countPaidBetween(start, end),
                orderRepository.countByStatus("PAID"),        // payées, en attente de validation
                orderRepository.countByStatus("PREPARING"),   // validées, à expédier
                supplierOrderRepository.countByStatus("TO_SEND"),
                variantRepository.countLowStock(lowStockThreshold),
                variantRepository.countOutOfStock()
        );
    }

    /** Performance sur une période, comparée à la période précédente de même durée. */
    @Transactional(readOnly = true)
    public DashboardResponse.Performance performance(String period) {
        int days = daysOf(period);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        LocalDateTime previousStart = start.minusDays(days);

        BigDecimal revenue = scale(orderRepository.revenueBetween(start, now));
        BigDecimal previousRevenue = scale(orderRepository.revenueBetween(previousStart, start));

        long orders = orderRepository.countPaidBetween(start, now);
        long previousOrders = orderRepository.countPaidBetween(previousStart, start);

        BigDecimal averageBasket = orders > 0
                ? revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardResponse.Performance(
                period,
                revenue,
                previousRevenue,
                changePercent(previousRevenue, revenue),
                orders,
                previousOrders,
                averageBasket,
                userRepository.countByCreatedAtBetween(start, now),
                orderRepository.countByStatusBetween("CANCELLED", start, now)
        );
    }

    /** Chiffre d'affaires jour par jour, jours sans vente inclus. */
    @Transactional(readOnly = true)
    public List<DashboardResponse.DailyPoint> dailyRevenue(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);

        List<Object[]> rows = orderRepository.dailyRevenueSince(from.atStartOfDay());

        List<DashboardResponse.DailyPoint> points = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate day = from.plusDays(i);

            BigDecimal revenue = BigDecimal.ZERO;
            long orders = 0;

            for (Object[] row : rows) {
                if (toLocalDate(row[0]).equals(day)) {
                    revenue = scale((BigDecimal) row[1]);
                    orders = ((Number) row[2]).longValue();
                    break;
                }
            }

            points.add(new DashboardResponse.DailyPoint(day, revenue, orders));
        }

        return points;
    }

    /**
     * Marge estimée à partir des coûts d'achat.
     * Signale explicitement si des coûts manquent, plutôt que d'afficher un chiffre faux.
     */
    @Transactional(readOnly = true)
    public DashboardResponse.Margins margins() {
        long missing = variantRepository.countWithoutCostPrice();

        BigDecimal revenue = scale(orderRepository.revenueBetween(
                LocalDate.now().minusDays(29).atStartOfDay(), LocalDateTime.now()));

        BigDecimal cost = scale(orderItemRepository.totalCostSince(
                LocalDate.now().minusDays(29).atStartOfDay()));

        BigDecimal margin = revenue.subtract(cost);

        Double percent = revenue.signum() > 0
                ? margin.divide(revenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : null;

        return new DashboardResponse.Margins(margin, percent, missing, missing == 0);
    }

    private int daysOf(String period) {
        if (period == null) return 30;
        return switch (period) {
            case "today" -> 1;
            case "7d" -> 7;
            case "90d" -> 90;
            default -> 30;
        };
    }

    private Double changePercent(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.signum() == 0) {
            return null; // pas de comparaison possible sans période de référence
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date date) return date.toLocalDate();
        if (value instanceof LocalDate date) return date;
        return LocalDate.parse(value.toString());
    }
        /** Listes exploitables du tableau de bord. */
    @Transactional(readOnly = true)
    public com.parfum.ecommerce.admin.dto.DashboardListsResponse lists(String period, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return new com.parfum.ecommerce.admin.dto.DashboardListsResponse(
                recentOrders(),
                topProducts(period, safeLimit),
                stockAlerts(safeLimit)
        );
    }

    @Transactional(readOnly = true)
    public List<com.parfum.ecommerce.admin.dto.DashboardListsResponse.RecentOrder> recentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(order -> new com.parfum.ecommerce.admin.dto.DashboardListsResponse.RecentOrder(
                        order.getId(),
                        order.getId().toString().substring(0, 8).toUpperCase(),
                        customerName(order),
                        order.getUser() != null ? order.getUser().getEmail() : null,
                        scale(order.getTotalAmount()),
                        order.getStatus(),
                        order.getCreatedAt(),
                        order.getItems().stream().anyMatch(
                                i -> "DROPSHIP".equals(i.getProduct().getFulfillmentType()))
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.parfum.ecommerce.admin.dto.DashboardListsResponse.TopProduct> topProducts(String period, int limit) {
        LocalDateTime since = LocalDate.now().minusDays(daysOf(period) - 1L).atStartOfDay();

        return orderItemRepository.topProductsSince(
                        since, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(row -> new com.parfum.ecommerce.admin.dto.DashboardListsResponse.TopProduct(
                        (java.util.UUID) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        scale((BigDecimal) row[3])
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.parfum.ecommerce.admin.dto.DashboardListsResponse.StockAlert> stockAlerts(int limit) {
        return variantRepository.findLowStock(
                        lowStockThreshold, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(v -> new com.parfum.ecommerce.admin.dto.DashboardListsResponse.StockAlert(
                        v.getProduct().getId(),
                        v.getId(),
                        v.getProduct().getName(),
                        v.getLabel(),
                        v.getStockQuantity() != null ? v.getStockQuantity() : 0,
                        v.getStockQuantity() == null || v.getStockQuantity() == 0
                ))
                .toList();
    }

    private String customerName(com.parfum.ecommerce.order.Order order) {
        var address = order.getAddress();
        if (address != null && address.getFirstName() != null) {
            return (address.getFirstName() + " "
                    + (address.getLastName() != null ? address.getLastName() : "")).trim();
        }
        if (order.getUser() != null) {
            String first = order.getUser().getFirstName();
            String last = order.getUser().getLastName();
            String name = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
            return name.isEmpty() ? order.getUser().getEmail() : name;
        }
        return "Client";
    }
}