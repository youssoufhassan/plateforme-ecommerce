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
    ],
  },
];
