package com.parfum.ecommerce.identity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    org.springframework.data.domain.Page<User> findByEmailContainingIgnoreCase(String email, org.springframework.data.domain.Pageable pageable);
    long countByCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);
}