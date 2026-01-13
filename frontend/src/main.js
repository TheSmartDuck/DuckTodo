import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/assets/css/theme.css'
import http from './utils/http'

Vue.config.productionTip = false

Vue.use(ElementUI)

// 注入 HTTP 封装，组件内可通过 this.$http 调用
Vue.prototype.$http = http

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
