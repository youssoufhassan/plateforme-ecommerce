package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final int VALIDITY_MINUTES = 60;
    private static final int COOLDOWN_MINUTES = 2;
    private static final int MAX_REQUESTS_PER_DAY = 5;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Demande de réinitialisation.
     * Ne lève JAMAIS d'erreur visible : la réponse doit être identique
     * que l'email existe ou non, pour ne pas révéler qui est inscrit.
     */
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        User user = userRepository.findByEmail(email.trim()).orElse(null);
        if (user == null) {
            log.info("Demande de reinitialisation pour un email inconnu (ignoree)");
            return;
        }

        boolean tooSoon = tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(last -> last.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(COOLDOWN_MINUTES)))
                .orElse(false);

        long requestsToday = tokenRepository.countByUserIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(24));

        if (tooSoon || requestsToday >= MAX_REQUESTS_PER_DAY) {
            log.info("Demande de reinitialisation limitee pour un utilisateur (ignoree)");
            return;
        }

        String rawToken = generateToken();
        tokenRepository.save(new PasswordResetToken(
                user, hash(rawToken), LocalDateTime.now().plusMinutes(VALIDITY_MINUTES)));

        String link = frontendUrl + "/reinitialiser-mot-de-passe?token=" + rawToken;

        emailService.send(user.getEmail(), "Réinitialisation de votre mot de passe",
                "reset-password", Map.of("link", link));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Lien de réinitialisation invalide"));

        if (token.getUsedAt() != null) {
            throw new IllegalStateException("Ce lien a déjà été utilisé");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce lien a expiré, faites une nouvelle demande");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setGuest(false);
        user.setFailedLoginCount(0);
user.setLockedUntil(null);
        userRepository.save(user);

        // Invalide ce jeton et tous les autres jetons encore actifs de cet utilisateur
        for (PasswordResetToken t : tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())) {
            t.setUsedAt(LocalDateTime.now());
            tokenRepository.save(t);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Hachage impossible", e);
        }
    }
}