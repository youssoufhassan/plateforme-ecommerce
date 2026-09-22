package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.identity.dto.ChangePasswordRequest;
import com.parfum.ecommerce.identity.dto.LoginRequest;
import com.parfum.ecommerce.identity.dto.RegisterRequest;
import com.parfum.ecommerce.identity.dto.ResetPasswordRequest;
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
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService,
                           EmailVerificationService emailVerificationService,
                           PasswordResetService passwordResetService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
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

    /** Réponse toujours identique, que l'email existe ou non. */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        passwordResetService.requestReset(body.get("email"));
        return ResponseEntity.ok(Map.of("message",
                "Si un compte existe avec cette adresse, un email de réinitialisation a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été modifié. Vous pouvez vous connecter."));
    }
}