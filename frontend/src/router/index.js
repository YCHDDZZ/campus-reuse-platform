import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import ProductListView from '../views/ProductListView.vue'
import ProductDetailView from '../views/ProductDetailView.vue'
import ProductFormView from '../views/ProductFormView.vue'
import MineView from '../views/MineView.vue'
import AdminView from '../views/AdminView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: ProductListView },
    { path: '/products/create', component: ProductFormView, meta: { auth: true } },
    { path: '/products/:id/edit', component: ProductFormView, meta: { auth: true } },
    { path: '/products/:id', component: ProductDetailView },
    { path: '/mine', component: MineView, meta: { auth: true } },
    { path: '/admin', component: AdminView, meta: { auth: true, admin: true } },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.auth && !authStore.isLogin) {
    ElMessage.warning('请先登录')
    return '/login'
  }
  if (to.meta.admin && !authStore.isAdmin) {
    ElMessage.error('仅管理员可访问')
    return '/'
  }
  return true
})

export default router
