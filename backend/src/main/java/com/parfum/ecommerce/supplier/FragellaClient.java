package com.parfum.ecommerce.supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FragellaClient {

    private final RestClient restClient;

    public FragellaClient(
            @Value("${fragella.api.base-url}") String baseUrl,
            @Value("${fragella.api.key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    public List<FragellaProduct> searchFragrances(String search, int limit) {
        List<FragellaProduct> products = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fragrances")
                        .queryParam("search", search)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FragellaProduct>>() {});

        return products != null ? products : List.of();
    }
}