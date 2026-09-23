package com.parfum.ecommerce.shipping;

import com.parfum.ecommerce.shipping.dto.CreateShipmentRequest;
import com.parfum.ecommerce.shipping.dto.ShipmentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ShipmentResponse create(@PathVariable UUID orderId,
                                    @Valid @RequestBody CreateShipmentRequest request) {
        return shipmentService.createShipment(orderId, request);
    }

    @GetMapping
    public ShipmentResponse get(@PathVariable UUID orderId) {
        return shipmentService.getShipment(orderId);
    }
}