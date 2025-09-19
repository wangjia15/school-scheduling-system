import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import 'vue-toastification/dist/index.css'
import Toast from 'vue-toastification'

const toastOptions = {
  timeout: 5000,
  closeOnClick: true,
  pauseOnHover: true,
  position: 'top-right',
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Toast, toastOptions)

app.mount('#app')