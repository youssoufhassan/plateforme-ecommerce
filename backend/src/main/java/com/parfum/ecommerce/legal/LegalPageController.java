package com.parfum.ecommerce.legal;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class LegalPageController {

    private final LegalPageRepository repository;

    public LegalPageController(LegalPageRepository repository) {
        this.repository = repository;
    }

    /** Public : liste des pages, sans leur contenu. */
    @GetMapping("/api/legal")
    public List<Map<String, Object>> list() {
        return repository.findAll().stream()
                .map(p -> Map.<String, Object>of(
                        "slug", p.getSlug(),
                        "title", p.getTitle(),
                        "version", p.getVersion(),
                        "updatedAt", p.getUpdatedAt()))
                .toList();
    }

    /** Public : contenu d'une page. */
    @GetMapping("/api/legal/{slug}")
    public LegalPage get(@PathVariable String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Page introuvable : " + slug));
    }

    /** Admin : chaque modification incrémente la version. */
    @PutMapping("/api/admin/legal/{slug}")
    @Transactional
    public LegalPage update(@PathVariable String slug, @RequestBody Map<String, String> body) {
        LegalPage page = repository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Page introuvable : " + slug));

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide");
        }

        if (body.get("title") != null && !body.get("title").isBlank()) {
            page.setTitle(body.get("title").trim());
        }
        page.setContent(content);
        page.setVersion(page.getVersion() + 1);
        page.setUpdatedAt(LocalDateTime.now());

        return repository.save(page);
    }
}