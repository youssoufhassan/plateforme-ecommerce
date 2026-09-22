package com.parfum.ecommerce.legal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LegalPageRepository extends JpaRepository<LegalPage, UUID> {
    Optional<LegalPage> findBySlug(String slug);
}