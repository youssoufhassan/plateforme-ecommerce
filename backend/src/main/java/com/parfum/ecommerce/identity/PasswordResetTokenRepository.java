package com.parfum.ecommerce.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);

    List<PasswordResetToken> findByUserIdAndUsedAtIsNull(UUID userId);
}