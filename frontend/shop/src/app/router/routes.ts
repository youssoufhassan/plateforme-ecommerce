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
      {
        path: "compte",
        component: () => import("@/pages/account/AccountPage.vue"),
        children: [
          {
            path: "",
            name: "account-orders",
            component: () => import("@/pages/account/OrdersSection.vue"),
          },
          {
            path: "informations",
            name: "account-profile",
            component: () => import("@/pages/account/ProfileSection.vue"),
          },
          {
            path: "adresses",
            name: "account-addresses",
            component: () => import("@/pages/account/AddressesSection.vue"),
          },
          {
            path: "securite",
            name: "account-security",
            component: () => import("@/pages/account/SecuritySection.vue"),
          },
          {
            path: "donnees",
            name: "account-privacy",
            component: () => import("@/pages/account/PrivacySection.vue"),
          },
        ],
      },
      {
        path: "legal/:slug",
        name: "legal",
        component: () => import("@/pages/legal/LegalPage.vue"),
      },
    ],
  },
];
