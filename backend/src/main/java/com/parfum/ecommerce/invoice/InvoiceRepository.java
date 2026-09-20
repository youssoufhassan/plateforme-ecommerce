package com.parfum.ecommerce.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    @Query(value = "SELECT nextval('invoice_sequence')", nativeQuery = true)
    Long nextSequenceNumber();
}