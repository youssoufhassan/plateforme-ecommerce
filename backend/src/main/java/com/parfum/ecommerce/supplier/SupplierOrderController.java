package com.parfum.ecommerce.supplier;

import com.parfum.ecommerce.supplier.dto.SupplierOrderResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/supplier-orders")
public class SupplierOrderController {

    private final SupplierOrderService service;

    public SupplierOrderController(SupplierOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<SupplierOrderResponse> getAll() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/pending")
    public List<SupplierOrderResponse> getPending() {
        return service.getPending().stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}/sent")
    public SupplierOrderResponse markAsSent(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return toResponse(service.markAsSent(id, body.get("externalReference")));
    }

    @PutMapping("/{id}/status")
    public SupplierOrderResponse updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return toResponse(service.updateStatus(id, body.get("status")));
    }

    private SupplierOrderResponse toResponse(SupplierOrder so) {
        List<SupplierOrderResponse.ItemDto> items = so.getItems().stream()
                .map(i -> new SupplierOrderResponse.ItemDto(
                        i.getProduct().getName(),
                        i.getSupplierSku(),
                        i.getQuantity(),
                        i.getUnitCost()))
                .toList();

        BigDecimal total = so.getItems().stream()
                .filter(i -> i.getUnitCost() != null)
                .map(i -> i.getUnitCost().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SupplierOrderResponse(
                so.getId(),
                so.getOrder().getId(),
                so.getSupplier().getName(),
                so.getStatus(),
                so.getExternalReference(),
                so.getCreatedAt(),
                so.getSentAt(),
                total,
                items
        );
    }
}