import { createRouter, createWebHistory } from "vue-router";

import { routes } from "./routes";
import { setupRouterGuards } from "./guards";

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }

    if (to.path !== from.path) {
      return {
        top: 0,
        behavior: "smooth",
      };
    }

    return {
      top: 0,
    };
  },
});

setupRouterGuards(router);

export default router;
