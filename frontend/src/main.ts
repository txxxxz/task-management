import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

import './assets/main.css'
import 'animate.css'
// 导入自定义的frappe-gantt样式文件，覆盖原始样式
import './styles/frappe-gantt-custom.scss'
// 导入自定义的gantt样式覆盖
import './styles/gantt-overrides.scss'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
