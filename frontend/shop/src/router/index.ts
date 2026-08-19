import { createRouter, createWebHistory } from "vue-router";
import ProductList from "../views/ProductList.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import CartView from "../views/CartView.vue";
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: ProductList },
    { path: "/login", component: Login },
    { path: "/register", component: Register },
    { path: "/cart", component: CartView },
  ],
});

export default router;
