package com.parfum.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    boolean existsByVariantId(UUID variantId);
        /** Produits les plus vendus sur les commandes réellement payées. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT oi.product.id FROM OrderItem oi
        WHERE oi.order.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED')
          AND oi.order.createdAt > :since
        GROUP BY oi.product.id
        ORDER BY SUM(oi.quantity) DESC
    """)
    java.util.List<java.util.UUID> findBestSellerIds(
            @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since,
            org.springframework.data.domain.Pageable pageable);
}