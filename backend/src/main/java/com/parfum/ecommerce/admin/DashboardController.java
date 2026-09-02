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
}