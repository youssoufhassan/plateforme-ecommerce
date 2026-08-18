package com.parfum.ecommerce.identity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Clé secrète pour signer les tokens — en dur pour l'instant, à déplacer en .env plus tard
    private final SecretKey key = Keys.hmacShaKeyFor(
        "changez-cette-cle-secrete-en-production-minimum-32-caracteres".getBytes()
    );

    private final long expirationMs = 24 * 60 * 60 * 1000; // 24h

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}