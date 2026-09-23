package com.parfum.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    List<Order> findTop5ByOrderByCreatedAtDesc();
    List<Order> findByStatusAndExpiresAtBefore(String status, java.time.LocalDateTime before);
    org.springframework.data.domain.Page<Order> findByStatus(String status, org.springframework.data.domain.Pageable pageable);
        @org.springframework.data.jpa.repository.Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
        WHERE o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED')
          AND o.createdAt >= :from AND o.createdAt < :to
    """)
    java.math.BigDecimal revenueBetween(
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED')
          AND o.createdAt >= :from AND o.createdAt < :to
    """)
    long countPaidBetween(
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.status = :status AND o.createdAt >= :from AND o.createdAt < :to
    """)
    long countByStatusBetween(
            @org.springframework.data.repository.query.Param("status") String status,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    long countByStatus(String status);

    /** Chiffre d'affaires jour par jour, pour la courbe d'évolution. */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT DATE(created_at) AS jour,
               COALESCE(SUM(total_amount), 0) AS revenu,
               COUNT(*) AS commandes
        FROM orders
        WHERE status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED')
          AND created_at >= :from
        GROUP BY DATE(created_at)
        ORDER BY jour
    """, nativeQuery = true)
    java.util.List<Object[]> dailyRevenueSince(
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from);
}