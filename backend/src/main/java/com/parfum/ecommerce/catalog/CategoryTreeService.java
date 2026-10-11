package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.CategoryNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryTreeService {

    private static final int MAX_DEPTH = 6;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryTreeService(CategoryRepository categoryRepository,
                                ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Arbre complet.
     * onlyFilled masque les branches sans produit disponible :
     * une rubrique vide dessert la boutique.
     */
    @Transactional(readOnly = true)
    public List<CategoryNode> tree(boolean onlyFilled) {
        return categoryRepository.findByParentIsNullOrderByPositionAscNameAsc().stream()
                .filter(category -> !onlyFilled || category.isActive())
                .map(category -> toNode(category, onlyFilled))
                .filter(node -> !onlyFilled || node.productCount() > 0)
                .toList();
    }

    @Transactional
    public CategoryNode create(String name, UUID parentId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        Category parent = parentId == null ? null : find(parentId);

        if (parent != null && parent.getDepth() + 1 >= MAX_DEPTH) {
            throw new IllegalArgumentException("Profondeur maximale atteinte (" + MAX_DEPTH + ")");
        }

        String slug = slugify(name);
        String path = parent == null ? slug : parent.getPath() + "/" + slug;

        if (categoryRepository.existsByPath(path)) {
            throw new IllegalArgumentException("Une catégorie existe déjà ici : " + path);
        }

        Category category = new Category(name.trim(), slug, parent);
        category.setPosition(nextPosition(parent));

        return toNode(categoryRepository.save(category), false);
    }

    /** Le chemin ne change pas : les liens existants restent valables. */
    @Transactional
    public CategoryNode rename(UUID id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        Category category = find(id);
        category.setName(name.trim());

        return toNode(categoryRepository.save(category), false);
    }

    /**
     * Déplace un nœud sous un autre parent.
     * Les chemins de toute sa descendance sont recalculés.
     */
    @Transactional
    public CategoryNode move(UUID id, UUID newParentId) {
        Category category = find(id);
        Category newParent = newParentId == null ? null : find(newParentId);

        if (newParent != null) {
            if (newParent.getId().equals(category.getId())) {
                throw new IllegalArgumentException("Une catégorie ne peut pas être sa propre parente");
            }
            if (newParent.getPath().startsWith(category.getPath() + "/")) {
                throw new IllegalArgumentException("Déplacement impossible : cela créerait une boucle");
            }
        }

        String oldPath = category.getPath();
        String newPath = newParent == null
                ? category.getSlug()
                : newParent.getPath() + "/" + category.getSlug();

        if (oldPath.equals(newPath)) {
            return toNode(category, false);
        }
        if (categoryRepository.existsByPath(newPath)) {
            throw new IllegalArgumentException("Une catégorie existe déjà ici : " + newPath);
        }

        // Les descendants sont lus AVANT la modification du parent
        List<Category> descendants = categoryRepository.findDescendants(oldPath);

        category.setParent(newParent);
        category.setPath(newPath);
        category.setPosition(nextPosition(newParent));
        categoryRepository.save(category);

        for (Category descendant : descendants) {
            descendant.setPath(newPath + descendant.getPath().substring(oldPath.length()));
            categoryRepository.save(descendant);
        }

        return toNode(category, false);
    }

    @Transactional
    public void reorder(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new IllegalArgumentException("Aucune catégorie fournie");
        }

        int position = 0;
        for (UUID id : orderedIds) {
            Category category = find(id);
            category.setPosition(position++);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public CategoryNode setActive(UUID id, boolean active) {
        Category category = find(id);
        category.setActive(active);
        return toNode(categoryRepository.save(category), false);
    }

    /** Suppression refusée tant que la branche n'est pas vide. */
    @Transactional
    public void delete(UUID id) {
        Category category = find(id);

        if (!categoryRepository.findDescendants(category.getPath()).isEmpty()) {
            throw new IllegalStateException("Cette catégorie contient des sous-catégories");
        }
        if (categoryRepository.countProductsInBranch(category.getPath()) > 0) {
            throw new IllegalStateException("Cette catégorie contient des produits");
        }

        categoryRepository.delete(category);
    }

    /** Fil d'Ariane : la chaîne des ancêtres, de la racine au nœud. */
    @Transactional(readOnly = true)
    public List<CategoryNode> breadcrumb(String path) {
        List<CategoryNode> trail = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String segment : path.split("/")) {
            if (!current.isEmpty()) current.append("/");
            current.append(segment);

            categoryRepository.findByPath(current.toString())
                    .ifPresent(category -> trail.add(toNode(category, false)));
        }
        return trail;
    }

    /** Remplace l'ensemble des rattachements d'un produit. */
    @Transactional
    public void setProductCategories(UUID productId, List<UUID> categoryIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        if (categoryIds == null || categoryIds.isEmpty()) {
            product.getCategories().clear();
            product.setCategory(null);
            productRepository.save(product);
            return;
        }

        var categories = new HashSet<>(categoryRepository.findAllById(categoryIds));

        if (categories.size() != new HashSet<>(categoryIds).size()) {
            throw new IllegalArgumentException("Une des catégories est introuvable");
        }

        product.setCategories(categories);

        // La première de la liste reste la catégorie principale
        categoryRepository.findById(categoryIds.get(0)).ifPresent(product::setCategory);

        productRepository.save(product);
    }

    // ===== Utilitaires =====

    private CategoryNode toNode(Category category, boolean onlyFilled) {
        List<CategoryNode> children = category.getChildren().stream()
                .filter(child -> !onlyFilled || child.isActive())
                .map(child -> toNode(child, onlyFilled))
                .filter(node -> !onlyFilled || node.productCount() > 0)
                .toList();

        return new CategoryNode(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getPath(),
                category.getDepth(),
                category.getPosition(),
                category.isActive(),
                categoryRepository.countProductsInBranch(category.getPath()),
                children);
    }

    private Category find(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));
    }

    private int nextPosition(Category parent) {
        List<Category> siblings = parent == null
                ? categoryRepository.findByParentIsNullOrderByPositionAscNameAsc()
                : parent.getChildren();

        return siblings.stream().mapToInt(Category::getPosition).max().orElse(-1) + 1;
    }

    private String slugify(String value) {
        String slug = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (slug.isBlank()) {
            throw new IllegalArgumentException("Nom de catégorie invalide");
        }
        return slug.length() > 80 ? slug.substring(0, 80) : slug;
    }
}