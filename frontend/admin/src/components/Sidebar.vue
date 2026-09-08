<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useRouter } from "vue-router";

const authStore = useAuthStore();
const router = useRouter();

const mobileOpen = ref(false);

const links = [
  {
    to: "/",
    label: "Tableau de bord",
    icon: "▦",
  },
  {
    to: "/products",
    label: "Produits",
    icon: "◇",
  },
  {
    to: "/categories",
    label: "Catégories",
    icon: "▤",
  },
  {
    to: "/orders",
    label: "Commandes",
    icon: "□",
  },
  {
    to: "/customers",
    label: "Clients",
    icon: "♙",
  },
];

function handleLogout() {
  authStore.logout();
  mobileOpen.value = false;
  router.push("/login");
}

function closeMobileMenu() {
  mobileOpen.value = false;
}
</script>

<template>
  <button
    class="mobile-toggle"
    :class="{ active: mobileOpen }"
    aria-label="Ouvrir le menu"
    @click="mobileOpen = !mobileOpen"
  >
    <span></span>
    <span></span>
    <span></span>
  </button>

  <aside class="admin-sidebar" :class="{ open: mobileOpen }">
    <div class="sidebar-inner">
      ```
      <!-- Logo -->
      <div class="sidebar-brand">
        <div class="brand-mark">S</div>

        <div class="brand-text">
          <strong>SHAHIN</strong>
          <span>ADMINISTRATION</span>
        </div>
      </div>

      <!-- Navigation -->
      <div class="nav-section">
        <span class="nav-title">MENU PRINCIPAL</span>

        <nav class="admin-nav">
          <router-link
            v-for="link in links"
            :key="link.to"
            :to="link.to"
            class="nav-link"
            active-class="active"
            @click="closeMobileMenu"
          >
            <span class="nav-icon">{{ link.icon }}</span>
            <span class="nav-label">{{ link.label }}</span>
          </router-link>
        </nav>
      </div>

      <!-- Bottom -->
      <div class="sidebar-bottom">
        <div class="sidebar-divider"></div>

        <div class="admin-profile">
          <div class="profile-avatar">A</div>

          <div class="profile-info">
            <strong>Administrateur</strong>
            <span>Compte admin</span>
          </div>
        </div>

        <button class="admin-logout" @click="handleLogout">
          <span class="logout-icon">↪</span>
          <span>Déconnexion</span>
        </button>
      </div>
    </div>
    ```
  </aside>

  <div v-if="mobileOpen" class="sidebar-overlay" @click="closeMobileMenu"></div>
</template>

<style scoped>
.admin-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 250px;
  height: 100vh;
  z-index: 200;
  background: #111214;
  color: #ffffff;
}

.sidebar-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px 16px 18px;
}

/* BRAND */

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 10px 30px;
}

.brand-mark {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 10px;
  background: #ffffff;
  color: #111214;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 900;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.brand-text strong {
  font-size: 1rem;
  letter-spacing: 0.08em;
}

.brand-text span {
  color: #777b83;
  font-size: 0.57rem;
  font-weight: 700;
  letter-spacing: 0.13em;
}

/* NAVIGATION */

.nav-section {
  flex: 1;
}

.nav-title {
  display: block;
  padding: 0 12px 10px;
  color: #686c74;
  font-size: 0.61rem;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.nav-link {
  position: relative;
  min-height: 46px;
  padding: 0 12px;
  border-radius: 9px;
  color: #92969e;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 13px;
  font-size: 0.88rem;
  font-weight: 500;
  transition:
    background 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease;
}

.nav-link:hover {
  color: #ffffff;
  background: #1b1d20;
}

.nav-link.active {
  color: #ffffff;
  background: #24272b;
}

.nav-link.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #ffffff;
}

.nav-icon {
  width: 21px;
  text-align: center;
  color: #777c85;
  font-size: 1rem;
  line-height: 1;
}

.nav-link.active .nav-icon,
.nav-link:hover .nav-icon {
  color: #ffffff;
}

.nav-label {
  white-space: nowrap;
}

/* BOTTOM */

.sidebar-bottom {
  margin-top: auto;
}

.sidebar-divider {
  height: 1px;
  margin: 0 8px 18px;
  background: #292c31;
}

.admin-profile {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 8px 10px 16px;
}

.profile-avatar {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #292c31;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 0.78rem;
  font-weight: 700;
}

.profile-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.profile-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.78rem;
  font-weight: 600;
}

.profile-info span {
  color: #6e737b;
  font-size: 0.66rem;
}

.admin-logout {
  width: 100%;
  min-height: 43px;
  padding: 0 12px;
  border: 1px solid #292c31;
  border-radius: 8px;
  background: transparent;
  color: #92969e;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font: inherit;
  font-size: 0.82rem;
  text-align: left;
  transition:
    background 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease;
}

.admin-logout:hover {
  background: #1b1d20;
  border-color: #383c42;
  color: #ffffff;
}

.logout-icon {
  font-size: 1rem;
}

/* MOBILE */

.mobile-toggle {
  display: none;
}

.sidebar-overlay {
  display: none;
}

@media (max-width: 900px) {
  .mobile-toggle {
    position: fixed;
    top: 12px;
    left: 14px;
    z-index: 300;
    width: 40px;
    height: 40px;
    padding: 0;
    border: 1px solid #e3e5e8;
    border-radius: 9px;
    background: #ffffff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    cursor: pointer;
  }

  .mobile-toggle span {
    width: 17px;
    height: 2px;
    border-radius: 2px;
    background: #171717;
    transition: transform 0.18s ease;
  }

  .mobile-toggle.active span:first-child {
    transform: translateY(6px) rotate(45deg);
  }

  .mobile-toggle.active span:nth-child(2) {
    opacity: 0;
  }

  .mobile-toggle.active span:last-child {
    transform: translateY(-6px) rotate(-45deg);
  }

  .admin-sidebar {
    left: -270px;
    width: 250px;
    transition: left 0.22s ease;
  }

  .admin-sidebar.open {
    left: 0;
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 190;
    background: rgba(0, 0, 0, 0.45);
    backdrop-filter: blur(2px);
  }
}
</style>
