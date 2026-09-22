package com.parfum.ecommerce.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierSyncLogRepository extends JpaRepository<SupplierSyncLog, UUID> {

    Page<SupplierSyncLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    Optional<SupplierSyncLog> findFirstBySupplierKeyOrderByStartedAtDesc(String supplierKey);
}