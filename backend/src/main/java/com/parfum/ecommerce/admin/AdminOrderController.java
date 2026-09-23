package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.admin.dto.OrderAdminDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminDetailService adminDetailService;

    public AdminOrderController(AdminDetailService adminDetailService) {
        this.adminDetailService = adminDetailService;
    }

    @GetMapping("/{orderId}")
    public OrderAdminDetailResponse detail(@PathVariable UUID orderId) {
        return adminDetailService.orderDetail(orderId);
    }
}