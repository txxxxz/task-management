import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

import './assets/main.css'
import 'animate.css'
import './styles/frappe-gantt-custom.scss'

import './styles/gantt-overrides.scss'

// import notification store
import { useNotificationStore } from '@/stores/notification'

const app = createApp(App)

// register element plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

// preload notification count
const notificationStore = useNotificationStore()
notificationStore.getUnreadCount()



