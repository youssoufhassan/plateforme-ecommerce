package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AuthResponse;
import com.parfum.ecommerce.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class GuestAuthService {

    private static final Logger log = LoggerFactory.getLogger(GuestAuthService.class);

    private static final int CODE_VALIDITY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_CODES_PER_HOUR = 5;

    private final LoginCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public GuestAuthService(LoginCodeRepository codeRepository, UserRepository userRepository,
                             PasswordEncoder passwordEncoder, JwtService jwtService,
                             EmailService emailService) {
        this.codeRepository = codeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Envoie un code à 6 chiffres.
     * Ne lève jamais d'erreur visible : réponse identique dans tous les cas.
     */
    @Transactional
    public void requestCode(String rawEmail) {
        String email = normalize(rawEmail);
        if (email == null) return;

        boolean tooSoon = codeRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .map(last -> last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(COOLDOWN_SECONDS)))
                .orElse(false);

        long lastHour = codeRepository.countByEmailAndCreatedAtAfter(email, LocalDateTime.now().minusHours(1));

        if (tooSoon || lastHour >= MAX_CODES_PER_HOUR) {
            log.info("Demande de code limitee (ignoree)");
            return;
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        codeRepository.save(new LoginCode(email, hash(email, code),
                LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES)));

        emailService.send(email, "Votre code de confirmation SHAHIN", "login-code", Map.of("code", code));
    }

    /**
     * Vérifie le code. Crée un compte invité si l'email est inconnu,
     * sinon connecte au compte existant. Renvoie une session JWT.
     */
    @Transactional
    public AuthResponse verifyCode(String rawEmail, String code) {
        String email = normalize(rawEmail);
        if (email == null || code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code invalide");
        }

        LoginCode loginCode = codeRepository.findFirstByEmailAndUsedAtIsNullOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré"));

        if (loginCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce code a expiré, demandez-en un nouveau");
        }
        if (loginCode.getAttempts() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Trop de tentatives, demandez un nouveau code");
        }

        if (!loginCode.getCodeHash().equals(hash(email, code.trim()))) {
            loginCode.setAttempts(loginCode.getAttempts() + 1);
            codeRepository.save(loginCode);
            throw new IllegalArgumentException("Code incorrect");
        }

        loginCode.setUsedAt(LocalDateTime.now());
        codeRepository.save(loginCode);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User guest = new User(email, passwordEncoder.encode(UUID.randomUUID().toString()));
            guest.setGuest(true);
            return guest;
        });

        // Recevoir le code prouve la possession de l'adresse email
        user.setEmailVerified(true);
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    /** Transforme un compte invité en vrai compte en définissant un mot de passe. */
    @Transactional
    public AuthResponse setPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.isGuest()) {
            throw new IllegalStateException("Ce compte possède déjà un mot de passe");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setGuest(false);
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()));
    }

    private String normalize(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) return null;
        return email.trim();
    }

    private String hash(String email, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String value = email + ":" + code;
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Hachage impossible", e);
        }
    }
}