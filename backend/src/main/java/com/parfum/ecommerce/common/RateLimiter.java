package com.parfum.ecommerce.common;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compteur par fenêtre de temps fixe, en mémoire.
 * Adapté à un serveur unique ; au-delà, prévoir un stockage partagé (Redis).
 */
@Component
public class RateLimiter {

    private record Window(long startMillis, int count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    /** Enregistre une requête et renvoie true si elle reste sous la limite. */
    public boolean tryAcquire(String key, int limit, long windowMillis) {
        long now = System.currentTimeMillis();

        Window updated = windows.compute(key, (k, current) -> {
            if (current == null || now - current.startMillis() >= windowMillis) {
                return new Window(now, 1);
            }
            return new Window(current.startMillis(), current.count() + 1);
        });

        return updated.count() <= limit;
    }

    /** Secondes restantes avant la fin de la fenêtre (pour l'en-tête Retry-After). */
    public long secondsUntilReset(String key, long windowMillis) {
        Window w = windows.get(key);
        if (w == null) return 0;
        long remaining = windowMillis - (System.currentTimeMillis() - w.startMillis());
        return Math.max(1, remaining / 1000);
    }

    /** Nettoyage périodique des compteurs expirés, pour ne pas saturer la mémoire. */
    @Scheduled(fixedRate = 300_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().startMillis() > 600_000);
    }
}