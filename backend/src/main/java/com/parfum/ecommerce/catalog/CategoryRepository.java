package com.parfum.ecommerce.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  java.util.Optional<Category> findByName(String name);
}