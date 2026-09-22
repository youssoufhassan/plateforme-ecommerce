package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.common.TooManyRequestsException;
import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.identity.dto.ChangePasswordRequest;
import com.parfum.ecommerce.identity.dto.LoginRequest;
import com.parfum.ecommerce.identity.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailVerificationService.createAndSend(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    /**
     * Volontairement SANS @Transactional : le compteur d'échecs doit être
     * enregistré même quand on lève une exception (sinon il serait annulé).
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            // Même message qu'un mauvais mot de passe : ne pas révéler qui est inscrit
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        if (user.isLocked()) {
            throw new TooManyRequestsException(
                    "Trop de tentatives. Réessayez dans quelques minutes ou réinitialisez votre mot de passe.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        if (user.getFailedLoginCount() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginCount() + 1;

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            user.setFailedLoginCount(0);
        } else {
            user.setFailedLoginCount(attempts);
        }

        userRepository.save(user);
    }
}