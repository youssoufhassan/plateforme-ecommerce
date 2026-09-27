<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";

import { apiMessage } from "@/services/productService";
import { fetchLegalPage, fetchLegalPages } from "@/services/legalService";
import type { LegalPage, LegalPageSummary } from "@/types/legal";

const route = useRoute();

const page = ref<LegalPage | null>(null);
const pages = ref<LegalPageSummary[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

/** Le contenu est du texte brut : on le découpe en paragraphes lisibles. */
const paragraphs = computed(() =>
  (page.value?.content ?? "")
    .split(/\n{2,}/)
    .map((block) => block.trim())
    .filter(Boolean),
);

const updatedAt = computed(() => {
  if (!page.value) return "";
  return new Date(page.value.updatedAt).toLocaleDateString("fr-FR", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
});

/** Un contenu non rédigé ne doit pas passer pour un document valable. */
const isPlaceholder = computed(() =>
  (page.value?.content ?? "")
    .trim()
    .toLowerCase()
    .startsWith("contenu à rédiger"),
);

async function load(slug: string): Promise<void> {
  loading.value = true;
  error.value = null;

  try {
    page.value = await fetchLegalPage(slug);
  } catch (e: unknown) {
    error.value = apiMessage(e, "Cette page est introuvable.");
    page.value = null;
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.params.slug,
  (slug) => {
    if (typeof slug === "string") load(slug);
  },
  { immediate: true },
);

fetchLegalPages()
  .then((list) => (pages.value = list))
  .catch(() => {
    // La navigation latérale est un confort : son absence ne bloque rien
  });
</script>

<template>
  <main class="legal">
    <div class="container">
      <nav class="legal__breadcrumb" aria-label="Fil d'Ariane">
        <RouterLink to="/">Accueil</RouterLink>
        <span aria-hidden="true">/</span>
        <span>{{ page?.title ?? "Informations légales" }}</span>
      </nav>

      <div class="legal__layout">
        <!-- Navigation entre les pages légales -->
        <nav v-if="pages.length" class="legal__nav" aria-label="Pages légales">
          <RouterLink
            v-for="item in pages"
            :key="item.slug"
            :to="`/legal/${item.slug}`"
            class="legal__nav-link"
            :class="{
              'legal__nav-link--active': item.slug === route.params.slug,
            }"
          >
            {{ item.title }}
          </RouterLink>
        </nav>

        <article class="legal__content">
          <p v-if="loading" class="legal__state">Chargement</p>

          <template v-else-if="error">
            <p class="legal__state legal__state--error" role="alert">
              {{ error }}
            </p>
            <RouterLink to="/" class="legal__action"
              >Retour à l'accueil</RouterLink
            >
          </template>

          <template v-else-if="page">
            <h1 class="legal__title">{{ page.title }}</h1>

            <p class="legal__meta">
              Version {{ page.version }} — mise à jour le {{ updatedAt }}
            </p>

            <!-- Le contenu n'est pas encore rédigé : mieux vaut le dire -->
            <div v-if="isPlaceholder" class="legal__placeholder">
              <p>
                Ce document est en cours de rédaction. Pour toute question
                concernant vos droits ou vos commandes, contactez-nous
                directement.
              </p>
            </div>

            <div v-else class="legal__body">
              <p v-for="(paragraph, index) in paragraphs" :key="index">
                {{ paragraph }}
              </p>
            </div>
          </template>
        </article>
      </div>
    </div>
  </main>
</template>

<style scoped>
.legal {
  width: 100%;
  padding-bottom: 72px;
}

.legal__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-top: 22px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.legal__breadcrumb a {
  color: inherit;
  text-decoration: none;
}

.legal__breadcrumb a:hover {
  color: var(--color-text);
}

.legal__layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  padding-top: 26px;
}

/* =========================================================
   NAVIGATION
   ========================================================= */

.legal__nav {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  border-bottom: 1px solid var(--color-border);
  scrollbar-width: none;
}

.legal__nav::-webkit-scrollbar {
  display: none;
}

.legal__nav-link {
  flex-shrink: 0;
  padding: 10px 12px;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  color: var(--color-text-muted);
  font-size: 12.5px;
  text-decoration: none;
  white-space: nowrap;
  transition:
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.legal__nav-link:hover {
  color: var(--color-text);
}

.legal__nav-link--active {
  border-bottom-color: var(--color-text);
  color: var(--color-text);
}

/* =========================================================
   CONTENU
   ========================================================= */

.legal__content {
  min-width: 0;
}

.legal__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(26px, 3.2vw, 38px);
  font-weight: 400;
  line-height: 1.08;
  letter-spacing: -0.03em;
}

.legal__meta {
  margin: 10px 0 28px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.legal__body {
  max-width: 68ch;
}

.legal__body p {
  margin: 0 0 16px;
  color: var(--color-text);
  font-size: 14.5px;
  line-height: 1.7;
  /* Le texte brut du backend conserve ses retours à la ligne */
  white-space: pre-line;
}

.legal__body p:last-child {
  margin-bottom: 0;
}

.legal__placeholder {
  max-width: 62ch;
  padding: 18px 20px;
  border-left: 2px solid var(--color-text);
  background: #f5f4f1;
}

.legal__placeholder p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.6;
}

/* =========================================================
   ÉTATS
   ========================================================= */

.legal__state {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

.legal__state--error {
  color: var(--color-error);
}

.legal__action {
  display: inline-block;
  margin-top: 18px;
  padding: 11px 22px;
  border: 1px solid var(--color-text);
  color: var(--color-text);
  font-size: 12px;
  text-decoration: none;
}

.legal__action:hover {
  background: var(--color-text);
  color: var(--color-white);
}

/* =========================================================
   DESKTOP : navigation en colonne
   ========================================================= */

@media (min-width: 1001px) {
  .legal__layout {
    grid-template-columns: 220px minmax(0, 1fr);
    gap: 56px;
    align-items: start;
  }

  .legal__nav {
    position: sticky;
    top: calc(var(--header-height) + 26px);
    flex-direction: column;
    gap: 2px;
    overflow: visible;
    border-bottom: none;
    border-right: 1px solid var(--color-border);
    padding-right: 20px;
  }

  .legal__nav-link {
    padding: 8px 0;
    border-bottom: none;
    border-right: 2px solid transparent;
    margin-bottom: 0;
    margin-right: -22px;
    padding-right: 20px;
  }

  .legal__nav-link--active {
    border-right-color: var(--color-text);
  }
}
</style>
