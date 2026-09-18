package com.parfum.ecommerce.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  java.util.Optional<Payment> findByStripeSessionId(String stripeSessionId);
}