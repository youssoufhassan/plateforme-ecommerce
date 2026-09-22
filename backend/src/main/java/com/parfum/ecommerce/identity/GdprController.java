package com.parfum.ecommerce.identity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class GdprController {

    private final GdprService gdprService;

    public GdprController(GdprService gdprService) {
        this.gdprService = gdprService;
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> export(Authentication auth) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mes-donnees-shahin.json\"")
                .body(gdprService.exportData(auth.getName()));
    }

    @PutMapping("/consents")
    public ResponseEntity<Void> updateConsents(Authentication auth, @RequestBody Map<String, Boolean> body) {
        gdprService.updateMarketingConsent(auth.getName(), Boolean.TRUE.equals(body.get("marketing")));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(Authentication auth,
                                               @RequestBody(required = false) Map<String, String> body) {
        String password = body != null ? body.get("password") : null;
        gdprService.deleteAccount(auth.getName(), password);
        return ResponseEntity.noContent().build();
    }
}