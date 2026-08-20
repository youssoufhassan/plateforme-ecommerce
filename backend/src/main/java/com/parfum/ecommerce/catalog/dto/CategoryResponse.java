package com.parfum.ecommerce.catalog.dto;

import java.util.UUID;

public class CategoryResponse {
    private UUID id;
    private String name;

    public CategoryResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
}