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
      {
        path: "panier",
        name: "cart",
        component: () => import("@/pages/cart/CartPage.vue"),
      },
      {
        path: "connexion",
        name: "login",
        component: () => import("@/pages/auth/LoginPage.vue"),
      },
      {
        path: "mot-de-passe-oublie",
        name: "forgot-password",
        component: () => import("@/pages/auth/ForgotPasswordPage.vue"),
      },
      {
        path: "reinitialiser-mot-de-passe",
        name: "reset-password",
        component: () => import("@/pages/auth/ResetPasswordPage.vue"),
      },
      {
        path: "verifier-email",
        name: "verify-email",
        component: () => import("@/pages/auth/VerifyEmailPage.vue"),
      },
      {
        path: "commande",
        name: "checkout",
        component: () => import("@/pages/checkout/CheckoutPage.vue"),
      },
      {
        path: "commande/succes",
        name: "order-success",
        component: () => import("@/pages/checkout/OrderSuccessPage.vue"),
      },
    ],
  },
];
