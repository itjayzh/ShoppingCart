import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Cart from '../views/Cart.vue'
import ProductDetail from '../views/ProductDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home'},
    { path: '/home',name: 'home',component: Home,meta: { requiresAuth: true }},
    { path: '/login', name: 'login',component: Login },
    { path: '/register',name: 'register', component: Register },
    { path: '/cart',name: 'cart',component: Cart, meta: { requiresAuth: true }},
    { path: '/product/:id', name: 'product-detail', component: ProductDetail, meta: { requiresAuth: true } },
    { path: '/profile',  name: 'profile',  component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
    { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue'), meta: { requiresAuth: true } }
  ]
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 如果访问的是需要认证的页面且没有token
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } 
  // 如果已经登录且访问登录/注册页
  else if (token && (to.path === '/login' || to.path === '/register')) {
    next('/home')
  }
  else {
    next()
  }
})

export default router 