package com.parfum.ecommerce.supplier.dto;

import java.math.BigDecimal;
import java.util.List;

public class ImportSelectionRequest {

    /** Terme de recherche ayant produit les résultats : nécessaire pour les retrouver. */
    private String query;

    private List<Selection> items;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<Selection> getItems() { return items; }
    public void setItems(List<Selection> items) { this.items = items; }

    public static class Selection {
        private String externalId;
        /** Prix de vente choisi. Si absent, le prix suggéré est appliqué. */
        private BigDecimal price;
        private String categoryName;
        /** Rendre le produit visible immédiatement, ou le garder inactif le temps de le compléter. */
        private Boolean active;

        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}