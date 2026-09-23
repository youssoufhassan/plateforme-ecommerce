package com.parfum.ecommerce.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SectionRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150)
    private String title;

    @Size(max = 255)
    private String subtitle;

    /** Facultatif : généré à partir du titre s'il n'est pas fourni. */
    @Size(max = 80)
    private String slug;

    private Integer position;

    private Boolean active;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}