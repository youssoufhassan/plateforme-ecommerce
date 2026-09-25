import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./app/App.vue";
import router from "./app/router";

import "./styles/reset.css";
import "./styles/tokens.css";
import "./styles/typography.css";
import "./styles/globals.css";
import "./styles/utilities.css";

const app = createApp(App);

app.use(createPinia());
app.use(router);

app.mount("#app");
