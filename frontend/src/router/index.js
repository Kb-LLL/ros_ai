import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ChatWorkspace from '../components/ChatWorkspace.vue'
import KnowledgeBase from '../views/KnowledgeBase.vue'

const routes = [
  { path: '/login', component: Login, meta: { requiresGuest: true } },
  { path: '/register', component: Register, meta: { requiresGuest: true } },
  { path: '/', component: ChatWorkspace, meta: { requireAuth: true } },
  { path: '/knowledge', component: KnowledgeBase, meta: { requireAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('satoken')

  if (to.meta.requireAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.requiresGuest && token) {
    next('/')
    return
  }

  next()
})

export default router
