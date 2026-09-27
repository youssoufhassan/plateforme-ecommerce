import { ref } from "vue";

/**
 * Favoris conservés dans le navigateur, faute d'endpoint côté serveur.
 * Ils ne suivent donc pas le compte client et sont perdus si l'utilisateur
 * change d'appareil ou vide son stockage.
 */
const STORAGE_KEY = "shahin:favorites";

function readStored(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

// État partagé par toutes les cartes de la page
const favorites = ref<string[]>(readStored());

function persist(): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(favorites.value));
  } catch {
    // Stockage indisponible (navigation privée) : les favoris restent en mémoire
  }
}

export function useFavorites() {
  function isFavorite(productId: string): boolean {
    return favorites.value.includes(productId);
  }

  function toggleFavorite(productId: string, name: string): void {
    favorites.value = isFavorite(productId)
      ? favorites.value.filter((id) => id !== productId)
      : [...favorites.value, productId];

    persist();
  }

  return { favorites, isFavorite, toggleFavorite };
}
