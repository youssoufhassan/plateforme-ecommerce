package com.parfum.ecommerce.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LoginCodeRepository extends JpaRepository<LoginCode, UUID> {

    Optional<LoginCode> findFirstByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);

    Optional<LoginCode> findFirstByEmailOrderByCreatedAtDesc(String email);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}