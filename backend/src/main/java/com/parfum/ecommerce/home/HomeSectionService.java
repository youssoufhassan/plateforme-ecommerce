package com.parfum.ecommerce.home;

import com.parfum.ecommerce.catalog.Product;
import com.parfum.ecommerce.catalog.ProductRepository;
import com.parfum.ecommerce.catalog.ProductService;
import com.parfum.ecommerce.catalog.dto.ProductResponse;
import com.parfum.ecommerce.home.dto.SectionRequest;
import com.parfum.ecommerce.home.dto.SectionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HomeSectionService {

    private static final int MAX_PRODUCTS_PER_SECTION = 50;

    private final HomeSectionRepository sectionRepository;
    private final HomeSectionProductRepository sectionProductRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public HomeSectionService(HomeSectionRepository sectionRepository,
                               HomeSectionProductRepository sectionProductRepository,
                               ProductRepository productRepository,
                               ProductService productService) {
        this.sectionRepository = sectionRepository;
        this.sectionProductRepository = sectionProductRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    // ===== Boutique =====

    /**
     * Sections affichées sur la page d'accueil.
     * Une section sans produit disponible est masquée, pour éviter un bloc vide.
     */
    @Transactional(readOnly = true)
    public List<SectionResponse> publicSections(int previewSize) {
        int limit = Math.min(Math.max(previewSize, 1), 12);

        return sectionRepository.findByActiveTrueOrderByPositionAsc().stream()
                .map(section -> toResponse(section, limit, true))
                .filter(section -> !section.products().isEmpty())
                .toList();
    }

    /** Tous les produits d'une section, pour la page dédiée. */
    @Transactional(readOnly = true)
    public SectionResponse publicSection(String slug) {
        HomeSection section = sectionRepository.findBySlug(slug)
                .filter(HomeSection::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Section introuvable"));

        return toResponse(section, MAX_PRODUCTS_PER_SECTION, true);
    }

    // ===== Administration =====

    @Transactional(readOnly = true)
    public List<SectionResponse> adminSections() {
        return sectionRepository.findAllByOrderByPositionAsc().stream()
                .map(section -> toResponse(section, MAX_PRODUCTS_PER_SECTION, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public SectionResponse adminSection(UUID sectionId) {
        return toResponse(findSection(sectionId), MAX_PRODUCTS_PER_SECTION, false);
    }

    @Transactional
    public SectionResponse create(SectionRequest request) {
        String slug = slugOf(request.getSlug(), request.getTitle());

        if (sectionRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Une section porte déjà cet identifiant : " + slug);
        }

        HomeSection section = new HomeSection();
        section.setSlug(slug);
        section.setTitle(request.getTitle().trim());
        section.setSubtitle(blankToNull(request.getSubtitle()));
        section.setPosition(request.getPosition() != null ? request.getPosition() : nextPosition());
        section.setActive(request.getActive() == null || request.getActive());

        return toResponse(sectionRepository.save(section), MAX_PRODUCTS_PER_SECTION, false);
    }

    @Transactional
    public SectionResponse update(UUID sectionId, SectionRequest request) {
        HomeSection section = findSection(sectionId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            section.setTitle(request.getTitle().trim());
        }
        if (request.getSubtitle() != null) {
            section.setSubtitle(blankToNull(request.getSubtitle()));
        }
        if (request.getPosition() != null) {
            section.setPosition(request.getPosition());
        }
        if (request.getActive() != null) {
            section.setActive(request.getActive());
        }

        // Le slug n'est modifiable que si la nouvelle valeur est libre
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String slug = slugOf(request.getSlug(), section.getTitle());
            if (!slug.equals(section.getSlug())) {
                if (sectionRepository.existsBySlug(slug)) {
                    throw new IllegalArgumentException("Une section porte déjà cet identifiant : " + slug);
                }
                section.setSlug(slug);
            }
        }

        return toResponse(sectionRepository.save(section), MAX_PRODUCTS_PER_SECTION, false);
    }

    @Transactional
    public void delete(UUID sectionId) {
        sectionRepository.delete(findSection(sectionId));
    }

    /** Ajoute un ou plusieurs produits à la fin de la section. */
    @Transactional
    public SectionResponse addProducts(UUID sectionId, List<UUID> productIds) {
        HomeSection section = findSection(sectionId);

        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Aucun produit sélectionné");
        }

        int position = section.getProducts().size();

        for (UUID productId : productIds) {
            if (section.getProducts().size() >= MAX_PRODUCTS_PER_SECTION) {
                throw new IllegalStateException(
                        "Maximum " + MAX_PRODUCTS_PER_SECTION + " produits par section");
            }
            if (sectionProductRepository.existsBySectionIdAndProductId(sectionId, productId)) {
                continue; // déjà dans la section
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Produit introuvable : " + productId));

            HomeSectionProduct link = new HomeSectionProduct(section, product, position++);
            section.getProducts().add(link);
            sectionProductRepository.save(link);
        }

        return toResponse(section, MAX_PRODUCTS_PER_SECTION, false);
    }

    @Transactional
    public SectionResponse removeProduct(UUID sectionId, UUID productId) {
        HomeSection section = findSection(sectionId);

        sectionProductRepository.findBySectionIdAndProductId(sectionId, productId)
                .ifPresent(link -> {
                    section.getProducts().remove(link);
                    sectionProductRepository.delete(link);
                });

        return toResponse(section, MAX_PRODUCTS_PER_SECTION, false);
    }

    /** Réordonne les produits selon l'ordre des identifiants fournis. */
    @Transactional
    public SectionResponse reorderProducts(UUID sectionId, List<UUID> orderedProductIds) {
        HomeSection section = findSection(sectionId);

        if (orderedProductIds == null || orderedProductIds.size() != section.getProducts().size()) {
            throw new IllegalArgumentException("La liste doit contenir tous les produits de la section");
        }

        int position = 0;
        for (UUID productId : orderedProductIds) {
            HomeSectionProduct link = section.getProducts().stream()
                    .filter(p -> p.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Produit absent de la section"));

            link.setPosition(position++);
            sectionProductRepository.save(link);
        }

        return toResponse(section, MAX_PRODUCTS_PER_SECTION, false);
    }

    /** Réordonne les sections elles-mêmes. */
    @Transactional
    public List<SectionResponse> reorderSections(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new IllegalArgumentException("Aucune section fournie");
        }

        int position = 0;
        for (UUID id : orderedIds) {
            HomeSection section = findSection(id);
            section.setPosition(position++);
            sectionRepository.save(section);
        }

        return adminSections();
    }

    // ===== Utilitaires =====

    private SectionResponse toResponse(HomeSection section, int limit, boolean publicView) {
        List<Product> products = section.getProducts().stream()
                .map(HomeSectionProduct::getProduct)
                // Côté boutique, on n'affiche que ce qui est réellement achetable
                .filter(product -> !publicView || product.isAvailable())
                .toList();

        List<ProductResponse> preview = products.stream()
                .limit(limit)
                .map(productService::toResponse)
                .toList();

        return new SectionResponse(
                section.getId(),
                section.getSlug(),
                section.getTitle(),
                section.getSubtitle(),
                section.getPosition(),
                section.isActive(),
                products.size(),
                preview
        );
    }

    private HomeSection findSection(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section introuvable"));
    }

    private int nextPosition() {
        return sectionRepository.findAllByOrderByPositionAsc().stream()
                .mapToInt(HomeSection::getPosition)
                .max()
                .orElse(-1) + 1;
    }

    /** Transforme un titre en identifiant d'URL : "Parfums niche" devient "parfums-niche". */
    private String slugOf(String providedSlug, String title) {
        String source = (providedSlug != null && !providedSlug.isBlank()) ? providedSlug : title;

        String normalized = Normalizer.normalize(source.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Impossible de générer un identifiant pour cette section");
        }
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}