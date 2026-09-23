package com.parfum.ecommerce.payment;

import com.parfum.ecommerce.invoice.InvoiceService;
import com.parfum.ecommerce.mail.OrderMailService;
import com.parfum.ecommerce.order.Order;
import com.parfum.ecommerce.order.OrderRepository;
import com.parfum.ecommerce.order.OrderService;
import com.parfum.ecommerce.payment.dto.CheckoutSessionResponse;
import com.parfum.ecommerce.supplier.SupplierOrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final SupplierOrderService supplierOrderService;
    private final StripeService stripeService;
    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final OrderMailService orderMailService;

    public PaymentService(PaymentRepository paymentRepository,
                           OrderRepository orderRepository,
                           SupplierOrderService supplierOrderService,
                           StripeService stripeService,
                           OrderService orderService,
                           InvoiceService invoiceService,
                           OrderMailService orderMailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.supplierOrderService = supplierOrderService;
        this.stripeService = stripeService;
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.orderMailService = orderMailService;
    }

    /** Étape 1 : le client demande à payer, on crée une session Stripe. */
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Cette commande ne vous appartient pas");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Cette commande n'est plus en attente de paiement");
        }

        if (!stripeService.isConfigured()) {
            throw new IllegalStateException("Le paiement n'est pas configuré (clé Stripe manquante)");
        }

        try {
            Session session = stripeService.createCheckoutSession(order);

            Payment payment = new Payment(order, order.getTotalAmount());
            payment.setProvider("STRIPE");
            payment.setStatus("PENDING");
            payment.setStripeSessionId(session.getId());
            paymentRepository.save(payment);

            return new CheckoutSessionResponse(session.getId(), session.getUrl());

        } catch (StripeException e) {
            throw new IllegalStateException("Erreur lors de la création du paiement : " + e.getMessage());
        }
    }

    /**
     * Étape 2 : Stripe confirme le paiement via webhook.
     * C'est ici, et uniquement ici, qu'une commande devient réellement payée.
     */
    @Transactional
    public void confirmPayment(String stripeSessionId, String transactionReference) {
        Payment payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable pour cette session"));

        if ("SUCCESS".equals(payment.getStatus())) {
            return; // déjà traité — Stripe peut renvoyer le même événement plusieurs fois
        }

        payment.setStatus("SUCCESS");
        payment.setTransactionReference(transactionReference);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        String previousStatus = order.getStatus();

        order.setStatus("PAID");
        order.setExpiresAt(null);

        orderService.decrementStock(order);
        orderRepository.save(order);

        orderService.recordStatusChange(order, previousStatus, "PAID", "Paiement confirmé par Stripe");

        invoiceService.issueForOrder(order);

        supplierOrderService.generateForOrder(order);

        // En dernier : un échec d'email ne doit rien compromettre de ce qui précède
        orderMailService.sendOrderReceived(order);
    }

    @Transactional
    public void markFailed(String stripeSessionId) {
        paymentRepository.findByStripeSessionId(stripeSessionId).ifPresent(payment -> {
            if (!"SUCCESS".equals(payment.getStatus())) {
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
            }
        });
    }
}