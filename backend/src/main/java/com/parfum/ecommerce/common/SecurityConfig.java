package com.parfum.ecommerce.common;

import com.parfum.ecommerce.identity.JwtAuthFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            .cors(cors -> {})

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Authentification
                .requestMatchers("/api/auth/**").permitAll()

                // Images publiques
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/api/payments/webhook").permitAll()

                .requestMatchers(
    HttpMethod.GET,
    "/api/supplier/fragella/search"
).permitAll()

.requestMatchers(
    HttpMethod.POST,
    "/api/supplier/fragella/import"
).permitAll()

                // Administration
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/orders/*/shipment"
                ).hasRole("ADMIN")

                // Catalogue public
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/shipping/**").permitAll()
                .requestMatchers("/api/legal/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/orders/*/cancel").hasRole("ADMIN")
                // Adresses
                .requestMatchers("/api/addresses/**").authenticated()

                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Value("${app.cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
}
}