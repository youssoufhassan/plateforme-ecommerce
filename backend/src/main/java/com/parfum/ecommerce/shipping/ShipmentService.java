package com.parfum.ecommerce.shipping;

import com.parfum.ecommerce.mail.OrderMailService;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.order.OrderService;
import com.parfum.ecommerce.shipping.dto.CreateShipmentRequest;
import com.parfum.ecommerce.shipping.dto.ShipmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderMailService orderMailService;
    private final OrderService orderService;

    public ShipmentService(ShipmentRepository shipmentRepository,
                            OrderRepository orderRepository,
                            OrderMailService orderMailService,
                            OrderService orderService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.orderMailService = orderMailService;
        this.orderService = orderService;
    }

    /**
     * Enregistre l'expédition et passe la commande en SHIPPED.
     * Réutilisable pour corriger un numéro de suivi erroné.
     */
    @Transactional
    public ShipmentResponse createShipment(UUID orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElse(new Shipment(order));

        shipment.setCarrier(request.getCarrier());
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setShippedAt(LocalDateTime.now());

        shipmentRepository.save(shipment);

        String previousStatus = order.getStatus();
        order.setStatus("SHIPPED");
        orderRepository.save(order);

        // Pas de nouvelle ligne d'historique si la commande était déjà expédiée (correction de suivi)
        if (!"SHIPPED".equals(previousStatus)) {
            orderService.recordStatusChange(order, previousStatus, "SHIPPED",
                    request.getCarrier() + " — " + request.getTrackingNumber());
        }

        orderMailService.sendOrderShipped(order, request.getCarrier(), request.getTrackingNumber());

        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune expédition pour cette commande"));
        return toResponse(shipment);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getShippedAt());
    }
}