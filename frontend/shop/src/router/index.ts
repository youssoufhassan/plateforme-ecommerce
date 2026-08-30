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
    { path: "/", component: Home },
    { path: "/produits", component: ProductList },
    { path: "/login", component: Login },
    { path: "/account", component: Account },
    { path: "/register", component: Register },
    { path: "/cart", component: CartView },
    { path: "/produits/:id", component: ProductDetail },
  ],
});

export default router;
