package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.cart.CartRepository;
import com.parfum.ecommerce.order.OrderService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GdprService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    public GdprService(UserRepository userRepository, AddressRepository addressRepository,
                        CartRepository cartRepository, LoginCodeRepository loginCodeRepository,
                        OrderService orderService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.loginCodeRepository = loginCodeRepository;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Droit d'accès : export de toutes les données personnelles du client. */
    @Transactional(readOnly = true)
    public Map<String, Object> exportData(String email) {
        User user = findUser(email);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phone", user.getPhone());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("guest", user.isGuest());
        profile.put("emailVerified", user.isEmailVerified());
        profile.put("marketingConsent", user.isMarketingConsent());
        profile.put("marketingConsentAt", user.getMarketingConsentAt());

        Map<String, Object> export = new LinkedHashMap<>();
        export.put("exportedAt", LocalDateTime.now());
        export.put("profile", profile);
        export.put("addresses", addressRepository.findByUserEmail(email).stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("firstName", a.getFirstName());
                    m.put("lastName", a.getLastName());
                    m.put("street", a.getStreet());
                    m.put("complement", a.getComplement());
                    m.put("city", a.getCity());
                    m.put("postalCode", a.getPostalCode());
                    m.put("countryCode", a.getCountryCode());
                    m.put("phone", a.getPhone());
                    return m;
                })
                .toList());
        export.put("orders", orderService.getMyOrders(email));

        return export;
    }

    /** Consentement marketing : modifiable à tout moment, avec date de preuve. */
    @Transactional
    public void updateMarketingConsent(String email, boolean consent) {
        User user = findUser(email);
        user.setMarketingConsent(consent);
        user.setMarketingConsentAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Droit à l'effacement, par anonymisation.
     * Les commandes et factures doivent être conservées 10 ans (obligation comptable),
     * ce que le RGPD autorise au titre d'une obligation légale.
     */
    @Transactional
    public void deleteAccount(String email, String password) {
        User user = findUser(email);

        // Un compte invité n'a pas de mot de passe connu : il a prouvé son email par code
        if (!user.isGuest()) {
            if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new IllegalArgumentException("Mot de passe incorrect");
            }
        }

        String originalEmail = user.getEmail();
        String anonymousId = UUID.randomUUID().toString();

        addressRepository.findByUserEmail(originalEmail).forEach(a -> {
            a.setFirstName(null);
            a.setLastName(null);
            a.setPhone(null);
            a.setStreet("Adresse supprimée");
            a.setComplement(null);
            addressRepository.save(a);
        });

        cartRepository.findByUserEmail(originalEmail).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });

        loginCodeRepository.deleteByEmail(originalEmail);

        user.setEmail("supprime-" + anonymousId + "@anonyme.local");
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhone(null);
        user.setMarketingConsent(false);
        user.setMarketingConsentAt(null);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }
}