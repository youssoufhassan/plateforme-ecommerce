package com.parfum.ecommerce.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupplierOrderRepository extends JpaRepository<SupplierOrder, UUID> {
    List<SupplierOrder> findByStatusOrderByCreatedAtAsc(String status);
    List<SupplierOrder> findByOrderId(UUID orderId);
        long countByStatus(String status);
}