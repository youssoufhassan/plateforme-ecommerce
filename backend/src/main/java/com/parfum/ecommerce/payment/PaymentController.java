package com.parfum.ecommerce.payment;

import com.parfum.ecommerce.payment.dto.PaymentResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/pay")
    public PaymentResponse pay(@PathVariable UUID orderId, Authentication auth) {
        return paymentService.pay(orderId, auth.getName());
    }
}