package com.parfum.ecommerce.supplier.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.parfum.ecommerce.supplier.ExternalProduct;
import com.parfum.ecommerce.supplier.SupplierAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class FragellaAdapter implements SupplierAdapter {

    private static final String BASE_URL =
            "https://api.fragella.com/api/v1/fragrances";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public FragellaAdapter(
            RestTemplate restTemplate,
            @Value("${fragella.api-key:}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public String getSupplierKey() {
        return "fragella";
    }

    @Override
    public String getSupplierName() {
        return "Fragella";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public List<ExternalProduct> fetchProducts(String query, int limit) {

        if (!isAvailable()) {
            throw new IllegalStateException(
                    "Fragella API key non configurée"
            );
        }

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Fragella nécessite un terme de recherche"
            );
        }

        int safeLimit = Math.min(Math.max(limit, 1), 10);

        String url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("search", query)
                .queryParam("limit", safeLimit)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        ResponseEntity<FragellaItem[]> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        FragellaItem[].class
                );

        FragellaItem[] items = response.getBody();

        if (items == null || items.length == 0) {
            return List.of();
        }

        List<ExternalProduct> result = new ArrayList<>();

        for (FragellaItem item : items) {

            if (item.name == null || item.name.isBlank()) {
                continue;
            }

            String externalId =
                    item.id != null
                            ? item.id
                            : item.name
                                .replaceAll("\\s+", "-")
                                .toLowerCase();

            result.add(
                    new ExternalProduct(
                            "fragella-" + externalId,
                            item.name,
                            item.brand,
                            buildDescription(item),
                            parsePrice(item.price),
                            null,
                            item.imageUrl,
                            List.of(),
                            mapCategory(item.gender)
                    )
            );
        }

        return result;
    }

    private BigDecimal parsePrice(String price) {

        if (price == null || price.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(price);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildDescription(FragellaItem item) {

        StringBuilder sb = new StringBuilder();

        if (item.brand != null) {
            sb.append(item.brand);
        }

        if (item.year != null) {
            appendSeparator(sb);
            sb.append("Lancé en ").append(item.year);
        }

        if (item.oilType != null && !item.oilType.isBlank()) {
            appendSeparator(sb);
            sb.append(item.oilType);
        }

        if (item.longevity != null && !item.longevity.isBlank()) {
            appendSeparator(sb);
            sb.append("Longévité : ").append(item.longevity);
        }

        if (item.sillage != null && !item.sillage.isBlank()) {
            appendSeparator(sb);
            sb.append("Sillage : ").append(item.sillage);
        }

        return sb.length() > 0
                ? sb.toString()
                : "Description à compléter.";
    }

    private void appendSeparator(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(" · ");
        }
    }

    private String mapCategory(String gender) {

        if (gender == null) {
            return "Parfums";
        }

        String g = gender.toLowerCase();

        if (g.contains("men") && !g.contains("women")) {
            return "Homme";
        }

        if (g.contains("women")) {
            return "Femme";
        }

        return "Unisexe";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FragellaItem {

        @JsonProperty("_id")
        public String id;

        @JsonProperty("Name")
        public String name;

        @JsonProperty("Brand")
        public String brand;

        @JsonProperty("Image URL")
        public String imageUrl;

        @JsonProperty("Gender")
        public String gender;

        @JsonProperty("Price")
        public String price;

        @JsonProperty("OilType")
        public String oilType;

        @JsonProperty("Longevity")
        public String longevity;

        @JsonProperty("Sillage")
        public String sillage;

        @JsonProperty("Year")
        public String year;
    }
}