package com.parfum.ecommerce.order;

import com.parfum.ecommerce.common.dto.PageResponse;
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
        boolean acceptTerms = false;

        if (body != null) {
            if (body.get("addressId") != null) {
                addressId = UUID.fromString(body.get("addressId").toString());
            }
            acceptTerms = Boolean.TRUE.equals(body.get("acceptTerms"));
        }

        return orderService.checkout(auth.getName(), addressId, acceptTerms);
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(Authentication auth) {
        return orderService.getMyOrders(auth.getName());
    }

    @GetMapping("/admin")
    public List<OrderResponse> getAllOrdersAdmin() {
        return orderService.getAllOrders();
    }

    @GetMapping("/admin/page")
    public PageResponse<OrderResponse> getOrdersPage(@RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return orderService.getOrdersPage(status, page, size);
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