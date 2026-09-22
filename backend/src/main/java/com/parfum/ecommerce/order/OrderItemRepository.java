package com.parfum.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    boolean existsByVariantId(UUID variantId);
}