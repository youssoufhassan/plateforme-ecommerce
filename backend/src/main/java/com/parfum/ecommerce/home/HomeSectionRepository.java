package com.parfum.ecommerce.home;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HomeSectionRepository extends JpaRepository<HomeSection, UUID> {

    List<HomeSection> findByActiveTrueOrderByPositionAsc();

    List<HomeSection> findAllByOrderByPositionAsc();

    Optional<HomeSection> findBySlug(String slug);

    boolean existsBySlug(String slug);
}