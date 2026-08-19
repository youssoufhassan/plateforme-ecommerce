package com.parfum.ecommerce.order;

import com.parfum.ecommerce.order.dto.OrderResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(Authentication auth) {
        return orderService.checkout(auth.getName());
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(Authentication auth) {
        return orderService.getMyOrders(auth.getName());
    }
}