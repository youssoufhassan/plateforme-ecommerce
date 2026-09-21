package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.identity.dto.ChangePasswordRequest;
import com.parfum.ecommerce.identity.dto.LoginRequest;
import com.parfum.ecommerce.identity.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(Authentication auth,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        if (auth == null) {
            throw new SecurityException("Connexion requise");
        }
        authService.changePassword(auth.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> body) {
        emailVerificationService.verify(body.get("token"));
        return ResponseEntity.ok(Map.of("message", "Votre adresse email a été vérifiée"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(Authentication auth) {
        if (auth == null) {
            throw new SecurityException("Connexion requise");
        }
        emailVerificationService.resend(auth.getName());
        return ResponseEntity.noContent().build();
    }
}