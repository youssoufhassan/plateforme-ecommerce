package com.parfum.ecommerce.order;

import com.parfum.ecommerce.order.dto.OrderResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

   @PostMapping("/checkout")
public OrderResponse checkout(Authentication auth, @RequestBody(required = false) java.util.Map<String, String> body) {
    UUID addressId = (body != null && body.get("addressId") != null) ? UUID.fromString(body.get("addressId")) : null;
    return orderService.checkout(auth.getName(), addressId);
}

    @GetMapping
    public List<OrderResponse> getMyOrders(Authentication auth) {
        return orderService.getMyOrders(auth.getName());
    }

    public record CheckoutRequest(UUID addressId) {
    }
    @GetMapping("/admin")
public List<OrderResponse> getAllOrdersAdmin() {
    return orderService.getAllOrders();
}
@PutMapping("/{orderId}/cancel")
public OrderResponse cancelOrder(@PathVariable UUID orderId) {
    return orderService.cancelOrder(orderId);
}
}
