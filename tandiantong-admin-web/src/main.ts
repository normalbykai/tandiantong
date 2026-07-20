import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/tokens.css'
import './styles/base.css'
import './styles/admin.css'
import './styles/platform-access.css'
import './styles/refinements.css'
import './styles/login.css'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

createApp(App).use(pinia).use(ElementPlus).use(router).mount('#app')
