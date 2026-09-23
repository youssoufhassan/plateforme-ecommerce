package com.parfum.ecommerce.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Vérifie qu'un fichier envoyé est bien une image acceptable.
 * Le type déclaré par le navigateur n'est pas fiable : on contrôle
 * aussi les premiers octets du fichier, qui identifient son format réel.
 */
public final class ImageValidator {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private ImageValidator() {}

        public static void validate(MultipartFile file, int maxSizeMb) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "Image trop lourde (" + (file.getSize() / 1024 / 1024) + " Mo). Maximum : " + maxSizeMb + " Mo");
        }

        // Seul le contenu réel fait foi : le type déclaré par le client n'est pas fiable
        // et certains outils n'en envoient aucun pour le format WebP.
        try {
            byte[] header = new byte[12];
            int read = file.getInputStream().read(header);

            if (read < 12 || detectFormat(header) == null) {
                throw new IllegalArgumentException(
                        "Ce fichier n'est pas une image valide. Formats acceptés : JPEG, PNG, WebP");
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier envoyé");
        }
    }
    /** Détecte le format réel d'après les premiers octets. */
    public static String detectFormat(byte[] header) {
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (header.length >= 8
                && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
            return "png";
        }
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        return null;
    }
}