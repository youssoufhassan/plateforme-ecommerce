package com.parfum.ecommerce.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long ONE_MINUTE = 60_000;
    private static final int AUTH_LIMIT = 10;
    private static final int GLOBAL_LIMIT = 300;

    /** Endpoints sensibles, ciblés par les attaques par force brute. */
    private static final List<String> SENSITIVE_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/guest/request-code",
            "/api/auth/guest/verify-code"
    );

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Le webhook Stripe reçoit des rafales légitimes depuis les serveurs de Stripe
        return !path.startsWith("/api/") || path.equals("/api/payments/webhook");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain chain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        if (SENSITIVE_PATHS.contains(path)) {
            String key = "auth:" + ip;
            if (!rateLimiter.tryAcquire(key, AUTH_LIMIT, ONE_MINUTE)) {
                reject(response, rateLimiter.secondsUntilReset(key, ONE_MINUTE),
                        "Trop de tentatives. Veuillez patienter avant de réessayer.");
                return;
            }
        }

        String globalKey = "global:" + ip;
        if (!rateLimiter.tryAcquire(globalKey, GLOBAL_LIMIT, ONE_MINUTE)) {
            reject(response, rateLimiter.secondsUntilReset(globalKey, ONE_MINUTE),
                    "Trop de requêtes. Veuillez patienter quelques instants.");
            return;
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, long retryAfter, String message) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}");
    }
}