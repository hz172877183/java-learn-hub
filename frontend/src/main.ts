/** 前端入口：注册 Pinia、路由与 Element Plus。 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles.css'

createApp(App).use(createPinia()).use(router).use(ElementPlus).mount('#app')
