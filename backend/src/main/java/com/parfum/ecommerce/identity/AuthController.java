package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.identity.dto.ChangePasswordRequest;
import com.parfum.ecommerce.identity.dto.LoginRequest;
import com.parfum.ecommerce.identity.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
public ResponseEntity<Void> changePassword(
        Authentication auth,
        @Valid @RequestBody ChangePasswordRequest request
) {
    authService.changePassword(auth.getName(), request);
    return ResponseEntity.noContent().build();
}
} 