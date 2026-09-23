package com.parfum.ecommerce.storage;

/**
 * Contrat commun à tous les modes de stockage d'images.
 * Passer du stockage local à un service externe ne demande
 * qu'une nouvelle implémentation, sans modifier le reste du code.
 */
public interface ImageStorage {

    /** Enregistre une image et renvoie sa référence et son URL publique. */
    StoredImage store(byte[] content, String originalFilename, String contentType);

    /** Supprime une image. Ne lève pas d'erreur si elle n'existe plus. */
    void delete(String reference);

    /** URL publique à partir d'une référence. */
    String publicUrl(String reference);
}