package com.parfum.ecommerce.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enregistre chaque modification effectuée par un administrateur.
 * Le contenu des requêtes n'est pas enregistré (données personnelles).
 */
@Component
public class AdminAuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditInterceptor.class);

    private final AdminAuditLogRepository repository;

    public AdminAuditInterceptor(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull Object handler, Exception ex) {

        String method = request.getMethod();
        if ("GET".equals(method) || "OPTIONS".equals(method) || "HEAD".equals(method)) {
            return; // seules les modifications sont journalisées
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        try {
            repository.save(new AdminAuditLog(
                    auth.getName(), method, request.getRequestURI(),
                    response.getStatus(), request.getRemoteAddr()));
        } catch (Exception e) {
            // Un échec de journalisation ne doit jamais faire échouer l'action elle-même
            log.error("Echec de la journalisation d'une action admin : {}", e.getMessage());
        }
    }
}