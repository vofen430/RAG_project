import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'
import { auth } from './auth'

import LoginView from './views/LoginView.vue'
import ChatView from './views/ChatView.vue'
import UploadView from './views/UploadView.vue'
import SettingsView from './views/SettingsView.vue'

const routes = [
  { path: '/login', name: 'Login', component: LoginView, meta: { public: true } },
  { path: '/', name: 'Chat', component: ChatView },
  { path: '/upload', name: 'Upload', component: UploadView },
  { path: '/settings', name: 'Settings', component: SettingsView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Auth guard
router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    // If already logged in, redirect to home
    if (auth.isLoggedIn && to.name === 'Login') {
      return next('/')
    }
    return next()
  }
  if (!auth.isLoggedIn) {
    return next('/login')
  }
  next()
})

const app = createApp(App)
app.use(router)
app.mount('#app')
