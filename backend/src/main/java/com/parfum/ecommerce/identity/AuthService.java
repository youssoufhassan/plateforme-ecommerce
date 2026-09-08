package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.identity.dto.ChangePasswordRequest;
import com.parfum.ecommerce.identity.dto.LoginRequest;
import com.parfum.ecommerce.identity.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        User user = new User(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
/* 
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }*/
    public void changePassword(String userEmail, ChangePasswordRequest request) {
    User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
        throw new IllegalArgumentException("Mot de passe actuel incorrect");
    }

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
}

public AuthResponse login(LoginRequest request) {

    System.out.println("🔥 LOGIN SERVICE APPELÉ");
    System.out.println("EMAIL = " + request.getEmail());

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException(
                    "Email ou mot de passe incorrect"
            ));

    System.out.println("🔥 USER TROUVÉ = " + user.getEmail());

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPasswordHash()
    )) {
        System.out.println("🔥 MOT DE PASSE INCORRECT");
        throw new IllegalArgumentException("Email ou mot de passe incorrect");
    }

    System.out.println("🔥 MOT DE PASSE CORRECT");

    String token = jwtService.generateToken(user.getEmail());

    return new AuthResponse(token);
}
}