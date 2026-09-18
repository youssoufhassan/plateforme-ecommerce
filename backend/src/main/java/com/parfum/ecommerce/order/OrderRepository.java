package com.parfum.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    List<Order> findTop5ByOrderByCreatedAtDesc();
    List<Order> findByStatusAndExpiresAtBefore(String status, java.time.LocalDateTime before);
}