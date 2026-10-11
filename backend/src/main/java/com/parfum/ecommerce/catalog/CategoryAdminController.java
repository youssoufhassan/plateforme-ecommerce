package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CategoryNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {

    private final CategoryTreeService treeService;

    public CategoryAdminController(CategoryTreeService treeService) {
        this.treeService = treeService;
    }

    /** Arbre complet, branches vides comprises. */
    @GetMapping("/tree")
    public List<CategoryNode> tree() {
        return treeService.tree(false);
    }

    @PostMapping
    public CategoryNode create(@RequestBody Map<String, String> body) {
        String parentId = body.get("parentId");
        return treeService.create(
                body.get("name"),
                (parentId == null || parentId.isBlank()) ? null : UUID.fromString(parentId));
    }

    /** Doit rester avant /{id} : sinon « order » serait pris pour un identifiant. */
    @PutMapping("/order")
    public ResponseEntity<Void> reorder(@RequestBody Map<String, List<UUID>> body) {
        treeService.reorder(body.get("categoryIds"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public CategoryNode rename(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return treeService.rename(id, body.get("name"));
    }

    @PutMapping("/{id}/move")
    public CategoryNode move(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String parentId = body.get("parentId");
        return treeService.move(id,
                (parentId == null || parentId.isBlank()) ? null : UUID.fromString(parentId));
    }

    @PutMapping("/{id}/active")
    public CategoryNode setActive(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        return treeService.setActive(id, Boolean.TRUE.equals(body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        treeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}