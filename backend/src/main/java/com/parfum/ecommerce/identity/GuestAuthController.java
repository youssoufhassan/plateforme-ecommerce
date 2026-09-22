package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/guest")
public class GuestAuthController {

    private final GuestAuthService guestAuthService;

    public GuestAuthController(GuestAuthService guestAuthService) {
        this.guestAuthService = guestAuthService;
    }

    @PostMapping("/request-code")
    public ResponseEntity<Map<String, String>> requestCode(@RequestBody Map<String, String> body) {
        guestAuthService.requestCode(body.get("email"));
        return ResponseEntity.ok(Map.of("message",
                "Si l'adresse est valide, un code de confirmation vient d'être envoyé."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<AuthResponse> verifyCode(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(guestAuthService.verifyCode(body.get("email"), body.get("code")));
    }

    @PostMapping("/set-password")
    public ResponseEntity<AuthResponse> setPassword(Authentication auth, @RequestBody Map<String, String> body) {
        if (auth == null) {
            throw new SecurityException("Connexion requise");
        }
        return ResponseEntity.ok(guestAuthService.setPassword(auth.getName(), body.get("password")));
    }
}