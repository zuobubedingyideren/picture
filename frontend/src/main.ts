import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import 'ant-design-vue/dist/reset.css'
import '@/access.ts'

// 抑制ResizeObserver警告的解决方案
// 重写ResizeObserver以捕获并忽略循环错误
if (typeof window !== 'undefined' && window.ResizeObserver) {
  const OriginalResizeObserver = window.ResizeObserver
  
  window.ResizeObserver = class extends OriginalResizeObserver {
    constructor(callback: ResizeObserverCallback) {
      const wrappedCallback: ResizeObserverCallback = (entries, observer) => {
        try {
          callback(entries, observer)
        } catch (error) {
          // 忽略ResizeObserver相关的错误
          if (error instanceof Error && 
              error.message.includes('ResizeObserver loop completed with undelivered notifications')) {
            return
          }
          throw error
        }
      }
      super(wrappedCallback)
    }
  }
}

// 全局错误处理，过滤ResizeObserver相关警告
const originalError = console.error
console.error = (...args) => {
  // 过滤ResizeObserver相关的错误信息
  const message = args[0]
  if (message && typeof message === 'string' && 
      message.includes('ResizeObserver loop completed with undelivered notifications')) {
    return
  }
  originalError.apply(console, args)
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)

app.mount('#app')
