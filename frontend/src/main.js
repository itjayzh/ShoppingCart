import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { User, Lock, Delete } from '@element-plus/icons-vue'
import router from './router'
import './style.css'
import App from './App.vue'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()

app.component('User', User)
app.component('Lock', Lock)
app.component('Delete', Delete)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化用户信息
const userStore = useUserStore(pinia)
userStore.init().finally(() => {
  app.mount('#app')
}) 