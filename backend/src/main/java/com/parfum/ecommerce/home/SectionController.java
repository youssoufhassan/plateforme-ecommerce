package com.parfum.ecommerce.home;

import com.parfum.ecommerce.home.dto.SectionResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final HomeSectionService sectionService;

    public SectionController(HomeSectionService sectionService) {
        this.sectionService = sectionService;
    }

    /** Sections de la page d'accueil, avec un aperçu de leurs produits. */
    @GetMapping
    public List<SectionResponse> sections(@RequestParam(defaultValue = "4") int preview) {
        return sectionService.publicSections(preview);
    }

    /** Tous les produits d'une section. */
    @GetMapping("/{slug}")
    public SectionResponse section(@PathVariable String slug) {
        return sectionService.publicSection(slug);
    }
}