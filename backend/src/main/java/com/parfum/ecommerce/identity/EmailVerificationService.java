package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.mail.EmailService;
import org.springframework.beans.factory.annotation.Value;
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
public class EmailVerificationService {

    private static final int VALIDITY_HOURS = 24;
    private static final int RESEND_COOLDOWN_MINUTES = 2;
    private static final int MAX_SENDS_PER_DAY = 5;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                     UserRepository userRepository,
                                     EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /** Génère un jeton, le stocke sous forme d'empreinte, et envoie le lien. */
    @Transactional
    public void createAndSend(User user) {
        String rawToken = generateToken();

        tokenRepository.save(new EmailVerificationToken(
                user, hash(rawToken), LocalDateTime.now().plusHours(VALIDITY_HOURS)));

        String link = frontendUrl + "/verifier-email?token=" + rawToken;

        emailService.send(user.getEmail(), "Confirmez votre adresse email", "verify-email",
                Map.of("link", link));
    }

    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Lien de vérification invalide");
        }

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Lien de vérification invalide"));

        if (token.getUsedAt() != null) {
            throw new IllegalStateException("Ce lien a déjà été utilisé");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce lien a expiré, demandez-en un nouveau");
        }

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /** Renvoi du lien, limité pour éviter les abus. */
    @Transactional
    public void resend(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Votre adresse email est déjà vérifiée");
        }

        tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).ifPresent(last -> {
            if (last.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(RESEND_COOLDOWN_MINUTES))) {
                throw new IllegalStateException(
                        "Veuillez patienter quelques minutes avant de demander un nouveau lien");
            }
        });

        long sentToday = tokenRepository.countByUserIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(24));
        if (sentToday >= MAX_SENDS_PER_DAY) {
            throw new IllegalStateException("Nombre maximum d'envois atteint, réessayez demain");
        }

        createAndSend(user);
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