package com.parfum.ecommerce.supplier;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier/fragella")
public class FragellaTestController {

    private final FragellaClient fragellaClient;

    public FragellaTestController(FragellaClient fragellaClient) {
        this.fragellaClient = fragellaClient;
    }

    @GetMapping("/search")
    public Object search(
            @RequestParam(defaultValue = "Sauvage") String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            return fragellaClient.searchFragrances(query, limit);
        } catch (Exception e) {
            e.printStackTrace();

            return java.util.Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "Aucun message",
                    "cause", e.getCause() != null
                            ? e.getCause().toString()
                            : "Aucune cause"
            );
        }
    }
}
