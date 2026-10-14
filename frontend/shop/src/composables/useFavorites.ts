import { computed, ref } from "vue";

/**
 * Favoris conservés dans le navigateur, faute d'endpoint côté serveur.
 * Ils ne suivent donc pas le compte client et sont perdus si l'utilisateur
 * change d'appareil ou vide son stockage.
 */
const STORAGE_KEY = "sidra:favorites";

function readStored(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

// État partagé par toutes les cartes et pages
const favorites = ref<string[]>(readStored());

function persist(): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(favorites.value));
  } catch {
    // Stockage indisponible (navigation privée) : les favoris restent en mémoire
  }
}

export function useFavorites() {
  const count = computed(() => favorites.value.length);

  function isFavorite(productId: string): boolean {
    return favorites.value.includes(productId);
  }

  function toggleFavorite(productId: string, _name?: string): void {
    favorites.value = isFavorite(productId)
      ? favorites.value.filter((id) => id !== productId)
      : [...favorites.value, productId];

    persist();
  }

  function removeFavorite(productId: string): void {
    favorites.value = favorites.value.filter((id) => id !== productId);
    persist();
  }

  function clearFavorites(): void {
    favorites.value = [];
    persist();
  }

  return {
    favorites,
    count,
    isFavorite,
    toggleFavorite,
    removeFavorite,
    clearFavorites,
  };
}
