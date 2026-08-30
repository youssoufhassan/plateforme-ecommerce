<template>
  <footer class="site-footer">
    <div class="footer-grid">
      <div>
        <h4 class="brand">Almass</h4>
        <p>Parfums et produits venus d'ailleurs, sélectionnés pour toi.</p>
      </div>
      <div>
        <h5>Boutique</h5>
        <router-link to="/produits">Tous les produits</router-link>
        <router-link to="/produits">Parfums</router-link>
      </div>
      <div>
        <h5>Compte</h5>
        <router-link to="/login">Connexion</router-link>
        <router-link to="/register">Créer un compte</router-link>
      </div>
    </div>
    <div class="footer-bottom">
      <span>&copy; 2026 Almass</span>
    </div>
  </footer>
</template>
