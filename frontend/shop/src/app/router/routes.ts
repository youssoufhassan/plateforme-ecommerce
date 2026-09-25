import type { RouteRecordRaw } from "vue-router";

import StoreLayout from "@/layouts/StoreLayout.vue";
import HomePage from "@/pages/home/HomePage.vue";

export const routes: RouteRecordRaw[] = [
  {
    path: "/",
    component: StoreLayout,
    children: [
      {
        path: "",
        name: "home",
        component: HomePage,
      },
      {
        path: "produits",
        name: "catalog",
        component: () => import("@/pages/catalog/CatalogPage.vue"),
      },
      {
        path: "produits/:id",
        name: "product",
        component: () => import("@/pages/product/ProductPage.vue"),
      },
      {
        path: "selection/:slug",
        name: "section",
        component: () => import("@/pages/catalog/SectionPage.vue"),
      },
    ],
  },
];
