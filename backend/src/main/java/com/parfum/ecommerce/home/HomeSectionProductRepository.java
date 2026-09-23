package com.parfum.ecommerce.home;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HomeSectionProductRepository extends JpaRepository<HomeSectionProduct, UUID> {
    Optional<HomeSectionProduct> findBySectionIdAndProductId(UUID sectionId, UUID productId);
    boolean existsBySectionIdAndProductId(UUID sectionId, UUID productId);
}