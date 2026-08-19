import { createRouter, createWebHistory } from "vue-router";
import ProductList from "../views/ProductList.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: ProductList },
    { path: "/login", component: Login },
    { path: "/register", component: Register },
  ],
});

export default router;
