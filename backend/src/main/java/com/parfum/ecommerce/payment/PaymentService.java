package com.parfum.ecommerce.payment;

import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse pay(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Cette commande ne vous appartient pas");
        }

        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Cette commande n'est plus en attente de paiement");
        }

        Payment payment = new Payment(order, order.getTotalAmount());

        // Simulation : 90% de succès, 10% d'échec (réaliste, imite un vrai prestataire)
        boolean success = Math.random() > 0.1;

        if (success) {
            payment.setStatus("SUCCESS");
            payment.setTransactionReference("MOCK-" + UUID.randomUUID());
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus("PAID");
        } else {
            payment.setStatus("FAILED");
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        if (!success) {
            throw new IllegalStateException("Le paiement a échoué, réessayez");
        }

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getStatus(),
            payment.getAmount(),
            payment.getTransactionReference(),
            payment.getPaidAt()
        );
    }
}