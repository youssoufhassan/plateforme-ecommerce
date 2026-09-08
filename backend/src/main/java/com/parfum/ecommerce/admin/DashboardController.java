package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.identity.UserRepository;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardController(OrderRepository orderRepository, UserRepository userRepository,
                                ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal revenue = orders.stream()
                .filter(o -> !o.getStatus().equals("PENDING"))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pending = orders.stream().filter(o -> o.getStatus().equals("PENDING")).count();

        return new DashboardStatsResponse(
                orders.size(),
                userRepository.count(),
                productRepository.count(),
                revenue,
                pending
        );
    }
    @GetMapping("/recent-orders")
public List<RecentOrderResponse> getRecentOrders() {

    return orderRepository.findTop5ByOrderByCreatedAtDesc()
            .stream()
            .map(order -> {

                String customerName = "Client";

                if (order.getUser() != null) {
                    String firstName = order.getUser().getFirstName();
                    String lastName = order.getUser().getLastName();

                    if (firstName != null && !firstName.isBlank()) {
                        customerName = firstName;

                        if (lastName != null && !lastName.isBlank()) {
                            customerName += " " + lastName;
                        }
                    } else if (order.getUser().getEmail() != null) {
                        customerName = order.getUser().getEmail();
                    }
                }

                String customerEmail =
                        order.getUser() != null
                                ? order.getUser().getEmail()
                                : null;

                return new RecentOrderResponse(
                        order.getId(),
                        customerName,
                        customerEmail,
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt()
                );
            })
            .toList();
}
@GetMapping("/revenue")
public List<RevenuePointResponse> getRevenue() {

    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDate startDate = today.minusDays(6);

    List<Order> orders = orderRepository.findAll();

    return java.util.stream.IntStream.rangeClosed(0, 6)
            .mapToObj(i -> {
                java.time.LocalDate date = startDate.plusDays(i);

                BigDecimal revenue = orders.stream()
                        .filter(order -> order.getCreatedAt() != null)
                        .filter(order -> order.getCreatedAt().toLocalDate().equals(date))
                        .filter(order -> !order.getStatus().equals("PENDING"))
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new RevenuePointResponse(date, revenue);
            })
            .toList();
}
}