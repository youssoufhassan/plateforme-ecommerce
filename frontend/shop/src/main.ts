import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./app/App.vue";
import router from "./app/router";
import { setUnauthorizedHandler } from "./services/http/api";
import { useAuthStore } from "./stores/authStore";

import "./styles/reset.css";
import "./styles/tokens.css";
import "./styles/typography.css";
import "./styles/globals.css";
import "./styles/utilities.css";

const app = createApp(App);

app.use(createPinia());
app.use(router);

const authStore = useAuthStore();

// Session expirée ou invalidée : on déconnecte et on renvoie vers la connexion
setUnauthorizedHandler(() => {
  authStore.logout();
  router.push("/connexion");
});

authStore.restore();

app.mount("#app");
