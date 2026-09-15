import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";
import ProductList from "../views/ProductList.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import CartView from "../views/CartView.vue";
import ProductDetail from "../views/ProductDetail.vue";
import Account from "../views/Account.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: Home },
    { path: "/produits", name: "products", component: ProductList },
    {
      path: "/produits/:id",
      name: "product-detail",
      component: ProductDetail,
      props: true,
    },
    { path: "/login", name: "login", component: Login, meta: { guestOnly: true } },
    { path: "/register", name: "register", component: Register, meta: { guestOnly: true } },
    {
      path: "/account",
      name: "account",
      component: Account,
      meta: { requiresAuth: true },
    },
    {
      path: "/cart",
      name: "cart",
      component: CartView,
      meta: { requiresAuth: true },
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      redirect: "/",
    },
  ],
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition;
    return { top: 0, behavior: "smooth" };
  },
});

router.beforeEach((to) => {
  const token = localStorage.getItem("token");
  const isAuthenticated = Boolean(token);

  if (to.meta.requiresAuth && !isAuthenticated) {
    return {
      name: "login",
      query: { redirect: to.fullPath },
    };
  }

  if (to.meta.guestOnly && isAuthenticated) {
    const redirect = typeof to.query.redirect === "string" ? to.query.redirect : "/";
    return redirect;
  }

  return true;
});

export default router;
