import type { Router } from "vue-router";

export function setupRouterGuards(router: Router) {
  router.beforeEach((to) => {
    return true;
  });
}
