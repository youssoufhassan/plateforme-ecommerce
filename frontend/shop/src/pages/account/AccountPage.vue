<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";

import { useAuthStore } from "@/stores/authStore";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const sections = [
  { name: "account-orders", path: "/compte", label: "Mes commandes" },
  {
    name: "account-profile",
    path: "/compte/informations",
    label: "Informations",
  },
  { name: "account-addresses", path: "/compte/adresses", label: "Adresses" },
  { name: "account-security", path: "/compte/securite", label: "Sécurité" },
  { name: "account-privacy", path: "/compte/donnees", label: "Mes données" },
];

const currentPath = computed(() => route.path);

function handleLogout(): void {
  authStore.logout();
  router.push("/");
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.replace("/connexion?redirect=/compte");
    return;
  }
  await authStore.loadProfile();
});
</script>

<template>
  <main class="account">
    <div class="container">
      <header class="account__header">
        <h1 class="account__title">Mon compte</h1>
        <p class="account__identity">{{ authStore.profile?.email }}</p>
      </header>

      <!-- Un compte invité n'a pas de mot de passe : on l'invite à en créer un -->
      <div v-if="authStore.isGuest" class="account__notice">
        <p>
          Vous avez commandé sans créer de compte. Choisissez un mot de passe
          pour retrouver vos commandes plus facilement.
        </p>
        <RouterLink to="/compte/securite" class="account__notice-action">
          Créer un mot de passe
        </RouterLink>
      </div>

      <div v-if="!authStore.emailVerified" class="account__notice">
        <p>
          Votre adresse email n'est pas encore confirmée. Vous ne pouvez pas
          commander.
        </p>
        <RouterLink to="/compte/securite" class="account__notice-action">
          Renvoyer l'email
        </RouterLink>
      </div>

      <div class="account__layout">
        <nav class="account__nav" aria-label="Sections du compte">
          <RouterLink
            v-for="section in sections"
            :key="section.path"
            :to="section.path"
            class="account__nav-link"
            :class="{
              'account__nav-link--active': currentPath === section.path,
            }"
          >
            {{ section.label }}
          </RouterLink>

          <button type="button" class="account__logout" @click="handleLogout">
            Se déconnecter
          </button>
        </nav>

        <div class="account__content">
          <RouterView />
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.account {
  width: 100%;
  padding-block: 28px 72px;
}

.account__header {
  margin-bottom: 26px;
}

.account__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(26px, 3.2vw, 38px);
  font-weight: 400;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.account__identity {
  margin: 8px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

.account__notice {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 15px 18px;
  margin-bottom: 20px;
  border-left: 2px solid var(--color-text);
  background: #f5f4f1;
}

.account__notice p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.5;
}

.account__notice-action {
  flex-shrink: 0;
  color: var(--color-text);
  font-size: 12.5px;
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* =========================================================
   NAVIGATION
   ========================================================= */

.account__layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 28px;
}

.account__nav {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  border-bottom: 1px solid var(--color-border);
  scrollbar-width: none;
}

.account__nav::-webkit-scrollbar {
  display: none;
}

.account__nav-link {
  flex-shrink: 0;
  padding: 11px 14px;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  color: var(--color-text-muted);
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
  transition:
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.account__nav-link:hover {
  color: var(--color-text);
}

.account__nav-link--active {
  border-bottom-color: var(--color-text);
  color: var(--color-text);
}

.account__logout {
  flex-shrink: 0;
  margin-left: auto;
  padding: 11px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  font-size: 13px;
  white-space: nowrap;
}

.account__logout:hover {
  color: var(--color-text);
}

.account__content {
  min-width: 0;
}

/* =========================================================
   DESKTOP : navigation en colonne
   ========================================================= */

@media (min-width: 1001px) {
  .account__layout {
    grid-template-columns: 210px minmax(0, 1fr);
    gap: 48px;
    align-items: start;
  }

  .account__nav {
    flex-direction: column;
    gap: 2px;
    overflow: visible;
    border-bottom: none;
    border-right: 1px solid var(--color-border);
    padding-right: 20px;
  }

  .account__nav-link {
    padding: 9px 0;
    border-bottom: none;
    border-right: 2px solid transparent;
    margin-bottom: 0;
    margin-right: -22px;
    padding-right: 20px;
  }

  .account__nav-link--active {
    border-right-color: var(--color-text);
  }

  .account__logout {
    margin: 14px 0 0;
    padding: 9px 0;
    text-align: left;
    border-top: 1px solid var(--color-border);
  }
}
</style>
