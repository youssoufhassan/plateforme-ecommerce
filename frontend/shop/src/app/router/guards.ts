import type { Router } from "vue-router";

export function setupRouterGuards(router: Router) {
  router.beforeEach((_to) => {
    return true;
  });
}
