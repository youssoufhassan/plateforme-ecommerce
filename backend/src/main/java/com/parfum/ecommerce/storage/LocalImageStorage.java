package com.parfum.ecommerce.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Stockage sur le disque du serveur.
 * Convient au développement. En production, les fichiers sont généralement
 * effacés à chaque redéploiement : prévoir un stockage externe.
 */
@Component
@ConditionalOnProperty(name = "app.images.storage", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalImageStorage.class);

    private final Path root;

    public LocalImageStorage(@Value("${app.images.local-dir}") String directory) {
        this.root = Paths.get(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            log.info("Images stockées localement dans {}", root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier d'images : " + root, e);
        }
    }

    @Override
    public StoredImage store(byte[] content, String originalFilename, String contentType) {
        String extension = extensionOf(content, contentType);

        // Rangement par mois pour éviter des milliers de fichiers dans un seul dossier
        String folder = LocalDate.now().toString().substring(0, 7);
        String filename = UUID.randomUUID() + "." + extension;
        String reference = folder + "/" + filename;

        try {
            Path target = root.resolve(reference).normalize();

            // Empêche toute écriture hors du dossier prévu
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Chemin de fichier invalide");
            }

            Files.createDirectories(target.getParent());
            Files.write(target, content);

            return new StoredImage(reference, publicUrl(reference));

        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer l'image : " + e.getMessage());
        }
    }

    @Override
    public void delete(String reference) {
        if (reference == null || reference.isBlank()) return;

        try {
            Path target = root.resolve(reference).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException e) {
            // Un fichier non supprimé ne doit pas bloquer l'action de l'administrateur
            log.warn("Suppression du fichier {} impossible : {}", reference, e.getMessage());
        }
    }

    @Override
    public String publicUrl(String reference) {
        return "/media/" + reference;
    }

    private String extensionOf(byte[] content, String contentType) {
        byte[] header = new byte[Math.min(12, content.length)];
        System.arraycopy(content, 0, header, 0, header.length);

        String detected = ImageValidator.detectFormat(header);
        if (detected != null) return detected;

        if (contentType == null) return "jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}