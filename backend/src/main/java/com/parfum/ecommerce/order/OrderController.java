package com.parfum.ecommerce.order;

import com.parfum.ecommerce.order.dto.OrderResponse;
import com.parfum.ecommerce.order.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(Authentication auth,
                                   @RequestBody(required = false) Map<String, Object> body) {
        UUID addressId = null;

        if (body != null && body.get("addressId") != null) {
            addressId = UUID.fromString(body.get("addressId").toString());
        }

        return orderService.checkout(auth.getName(), addressId);
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(Authentication auth) {
        return orderService.getMyOrders(auth.getName());
    }

    @GetMapping("/admin")
    public List<OrderResponse> getAllOrdersAdmin() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable UUID orderId,
                                       @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderId, request.getStatus());
    }

    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }
}