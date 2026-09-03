import { createRouter, createWebHistory } from "vue-router";
import AdminLayout from "../components/AdminLayout.vue";
import AdminLogin from "../views/AdminLogin.vue";
import Dashboard from "../views/Dashboard.vue";
import Products from "../views/Products.vue";
import Categories from "../views/Categories.vue";
import Orders from "../views/Orders.vue";
import Customers from "../views/Customers.vue";
import { useAuthStore } from "../stores/authStore";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: AdminLogin },
    {
      path: "/",
      component: AdminLayout,
      children: [
        { path: "", component: Dashboard },
        { path: "products", component: Products },
        { path: "categories", component: Categories },
        { path: "orders", component: Orders },
        { path: "customers", component: Customers },
      ],
    },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.path !== "/login" && !authStore.isLoggedIn()) {
    return "/login";
  }
});

export default router;
