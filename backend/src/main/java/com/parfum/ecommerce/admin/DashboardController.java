package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.admin.dto.DashboardResponse;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardController(DashboardService dashboardService,
                                OrderRepository orderRepository,
                                UserRepository userRepository,
                                ProductRepository productRepository) {
        this.dashboardService = dashboardService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /** Tableau de bord complet. period : today, 7d, 30d (défaut), 90d. */
    @GetMapping
    public DashboardResponse dashboard(@RequestParam(defaultValue = "30d") String period) {
        return dashboardService.dashboard(period);
    }

    @GetMapping("/today")
    public DashboardResponse.Today today() {
        return dashboardService.today();
    }

    @GetMapping("/revenue")
    public List<DashboardResponse.DailyPoint> revenue(@RequestParam(defaultValue = "30") int days) {
        return dashboardService.dailyRevenue(Math.min(Math.max(days, 7), 90));
    }

    /** Ancien endpoint, conservé pour ne pas casser le back-office existant. */
    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal revenue = orders.stream()
                .filter(o -> !"PENDING".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pending = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();

        return new DashboardStatsResponse(
                orders.size(),
                userRepository.count(),
                productRepository.count(),
                revenue,
                pending
        );
    }
}