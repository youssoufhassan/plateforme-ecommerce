package com.parfum.ecommerce.home;

import com.parfum.ecommerce.home.dto.SectionRequest;
import com.parfum.ecommerce.home.dto.SectionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/sections")
public class SectionAdminController {

    private final HomeSectionService sectionService;

    public SectionAdminController(HomeSectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public List<SectionResponse> list() {
        return sectionService.adminSections();
    }

    @PostMapping
    public SectionResponse create(@Valid @RequestBody SectionRequest request) {
        return sectionService.create(request);
    }

    @PutMapping("/order")
    public List<SectionResponse> reorderSections(@RequestBody Map<String, List<UUID>> body) {
        return sectionService.reorderSections(body.get("sectionIds"));
    }

    @GetMapping("/{sectionId}")
    public SectionResponse detail(@PathVariable UUID sectionId) {
        return sectionService.adminSection(sectionId);
    }

    @PutMapping("/{sectionId}")
    public SectionResponse update(@PathVariable UUID sectionId, @RequestBody SectionRequest request) {
        return sectionService.update(sectionId, request);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID sectionId) {
        sectionService.delete(sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sectionId}/products")
    public SectionResponse addProducts(@PathVariable UUID sectionId,
                                        @RequestBody Map<String, List<UUID>> body) {
        return sectionService.addProducts(sectionId, body.get("productIds"));
    }

    @PutMapping("/{sectionId}/products/order")
    public SectionResponse reorderProducts(@PathVariable UUID sectionId,
                                            @RequestBody Map<String, List<UUID>> body) {
        return sectionService.reorderProducts(sectionId, body.get("productIds"));
    }

    @DeleteMapping("/{sectionId}/products/{productId}")
    public SectionResponse removeProduct(@PathVariable UUID sectionId, @PathVariable UUID productId) {
        return sectionService.removeProduct(sectionId, productId);
    }
}