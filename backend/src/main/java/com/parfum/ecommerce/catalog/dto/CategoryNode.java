package com.parfum.ecommerce.catalog.dto;

import java.util.List;
import java.util.UUID;

/** Un nœud de l'arbre, avec ses enfants. */
public record CategoryNode(
        UUID id,
        String name,
        String slug,
        String path,
        int depth,
        int position,
        boolean active,
        long productCount,
        List<CategoryNode> children
) {}