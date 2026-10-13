package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductOptionValueRepository
        extends JpaRepository<ProductOptionValue, UUID> {
}