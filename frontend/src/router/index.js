import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ChatContainer from '../components/ChatContainer.vue'
import KnowledgeBase from '../views/KnowledgeBase.vue'

const routes = [
    { path: '/login',    component: Login,         meta: { requiresGuest: true } },
    { path: '/register', component: Register,      meta: { requiresGuest: true } },
    { path: '/',         component: ChatContainer, meta: { requireAuth: true } },
    { path: '/knowledge',component: KnowledgeBase, meta: { requireAuth: true } },  // ← 新增
    { path: '/:pathMatch(.*)*', redirect: '/' },
    {
        path: '/login',
        component: Login,
        meta: { requiresGuest: true }  // 已登录不能访问
    },
    {
        path: '/register',
        component: Register,
        meta: { requiresGuest: true }  // 已登录不能访问
    },
    {
        path: '/',
        component: ChatContainer,
        meta: { requireAuth: true }    // 未登录不能访问
    },
    {
        path: '/:pathMatch(.*)*',      // 404 重定向
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('satoken')

    if (to.meta.requireAuth && !token) {
        // 需要登录但没有 token → 跳登录页
        next('/login')
    } else if (to.meta.requiresGuest && token) {
        // 已登录但访问登录/注册页 → 跳首页
        next('/')
    } else {
        next()
    }
})

export default router