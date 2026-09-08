<script setup lang="ts">
import Sidebar from "./Sidebar.vue";
</script>

<template>
  <div class="admin-shell">
    <Sidebar />

    ```
    <main class="admin-content">
      <header class="admin-topbar">
        <div class="topbar-left">
          <div class="topbar-title">
            <span class="topbar-eyebrow">SHAHIN</span>
            <span class="topbar-separator">/</span>
            <span class="topbar-section">Administration</span>
          </div>
        </div>

        <div class="topbar-right">
          <div class="admin-status">
            <span class="status-dot"></span>
            <span>Administration</span>
          </div>

          <div class="admin-avatar">A</div>
        </div>
      </header>

      <section class="admin-page">
        <router-view />
      </section>
    </main>
    ```
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: #f6f7f9;
}

.admin-content {
  min-height: 100vh;
  margin-left: 250px;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  height: 72px;
  padding: 0 32px;
  background: #ffffff;
  border-bottom: 1px solid #e8eaee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.topbar-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
}

.topbar-eyebrow {
  font-weight: 800;
  letter-spacing: 0.08em;
  color: #171717;
}

.topbar-separator {
  color: #c4c7cc;
}

.topbar-section {
  color: #777d87;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.admin-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: #69707b;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.1);
}

.admin-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #171717;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
}

.admin-page {
  flex: 1;
  padding: 32px;
}

@media (max-width: 900px) {
  .admin-content {
    margin-left: 0;
  }

  .admin-topbar {
    height: 64px;
    padding: 0 18px 0 70px;
  }

  .topbar-section,
  .topbar-separator {
    display: none;
  }

  .admin-status {
    display: none;
  }

  .admin-page {
    padding: 24px 18px;
  }
}

@media (max-width: 480px) {
  .admin-topbar {
    padding-right: 14px;
  }

  .admin-page {
    padding: 20px 14px;
  }
}
</style>
