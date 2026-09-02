package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CategoryRequest;
import com.parfum.ecommerce.catalog.dto.CategoryResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }
    @PostMapping
public CategoryResponse create(@RequestBody CategoryRequest request) {
    Category category = new Category();
    category.setName(request.getName());
    Category saved = categoryRepository.save(category);
    return new CategoryResponse(saved.getId(), saved.getName());
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable UUID id) {
    categoryRepository.deleteById(id);
    return ResponseEntity.noContent().build();
}
}