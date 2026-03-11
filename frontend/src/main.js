import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

import ChatView from './views/ChatView.vue'
import UploadView from './views/UploadView.vue'
import SettingsView from './views/SettingsView.vue'

const routes = [
  { path: '/', name: 'Chat', component: ChatView },
  { path: '/upload', name: 'Upload', component: UploadView },
  { path: '/settings', name: 'Settings', component: SettingsView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const app = createApp(App)
app.use(router)
app.mount('#app')
