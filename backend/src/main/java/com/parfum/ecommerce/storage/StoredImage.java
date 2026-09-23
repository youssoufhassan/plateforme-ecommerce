package com.parfum.ecommerce.storage;

/**
 * @param reference identifiant interne, sert à supprimer le fichier
 * @param url adresse publique de l'image
 */
public record StoredImage(String reference, String url) {}