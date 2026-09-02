package com.parfum.ecommerce.shipping;

import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.shipping.dto.CreateShipmentRequest;
import com.parfum.ecommerce.shipping.dto.ShipmentResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    public ShipmentResponse createShipment(UUID orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElse(new Shipment(order));

        shipment.setCarrier(request.getCarrier());
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setShippedAt(LocalDateTime.now());

        shipmentRepository.save(shipment);

        order.setStatus("SHIPPED");
        orderRepository.save(order);

        return toResponse(shipment);
    }

    public ShipmentResponse getShipment(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune expédition pour cette commande"));
        return toResponse(shipment);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(shipment.getCarrier(), shipment.getTrackingNumber(), shipment.getShippedAt());
    }
}