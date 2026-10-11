package com.parfum.ecommerce.catalog;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    /**
     * Chemin complet depuis la racine : "homme/parfums/eaux-de-parfum".
     * Filtrer une branche entière devient un préfixe de chaîne,
     * sans requête récursive.
     */
    @Column(nullable = false, unique = true)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("position ASC, name ASC")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private Integer position = 0;

    @Column(nullable = false)
    private boolean active = true;

    public Category() {}

    public Category(String name, String slug, Category parent) {
        this.name = name;
        this.slug = slug;
        this.parent = parent;
        this.path = (parent == null) ? slug : parent.getPath() + "/" + slug;
    }

    /** Profondeur : 0 pour une racine. */
    public int getDepth() {
        return (int) path.chars().filter(c -> c == '/').count();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    public List<Category> getChildren() { return children; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}