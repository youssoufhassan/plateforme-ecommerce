package com.parfum.ecommerce.identity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService,
                          UserDetailsService userDetailsService,
                          UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email;
        Date issuedAt;

        try {
            email = jwtService.extractEmail(token);
            issuedAt = jwtService.extractIssuedAt(token);
        } catch (Exception e) {
            // Jeton invalide ou expiré : la requête continue sans authentification
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || isIssuedBeforePasswordChange(issuedAt, user)) {
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    /** Un jeton émis avant le dernier changement de mot de passe n'est plus accepté. */
    private boolean isIssuedBeforePasswordChange(Date issuedAt, User user) {
        if (user.getPasswordChangedAt() == null || issuedAt == null) {
            return false;
        }
        // La date d'émission d'un JWT est à la seconde près : on compare à la seconde
        Date changedAt = Date.from(user.getPasswordChangedAt()
                .truncatedTo(ChronoUnit.SECONDS)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        return issuedAt.before(changedAt);
    }
}