import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'

// SSO单点登录：从URL中提取token和用户信息并存储
const urlParams = new URLSearchParams(window.location.search)
const ssoToken = urlParams.get('sso_token')
if (ssoToken) {
  localStorage.setItem('sso_token', ssoToken)
}
const realName = urlParams.get('realName')
if (realName) {
  localStorage.setItem('sso_realName', realName)
}
// 清除URL中的参数
if (ssoToken || realName) {
  window.history.replaceState({}, document.title, window.location.pathname + window.location.hash)
}

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
