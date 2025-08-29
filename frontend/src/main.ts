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
// 使用更可靠的方法来处理ResizeObserver错误
if (typeof window !== 'undefined') {
  // 捕获并忽略ResizeObserver错误
  const resizeObserverErrorHandler = (e: ErrorEvent) => {
    if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
      e.stopImmediatePropagation()
      return false
    }
    return true
  }
  
  window.addEventListener('error', resizeObserverErrorHandler)
  
  // 同时处理未捕获的Promise错误
  window.addEventListener('unhandledrejection', (e) => {
    if (e.reason && e.reason.message && 
        e.reason.message.includes('ResizeObserver loop completed with undelivered notifications')) {
      e.preventDefault()
    }
  })
}

// 全局错误处理，过滤ResizeObserver相关警告
const originalError = console.error
console.error = (...args) => {
  // 过滤ResizeObserver相关的错误信息
  const message = args[0]
  if (message && typeof message === 'string' && 
      (message.includes('ResizeObserver loop completed with undelivered notifications') ||
       message.includes('ResizeObserver loop limit exceeded'))) {
    return
  }
  originalError.apply(console, args)
}

// 额外的ResizeObserver错误抑制
if (typeof window !== 'undefined') {
  const originalConsoleError = window.console.error
  window.console.error = function(...args) {
    if (args[0] && typeof args[0] === 'string' && 
        args[0].includes('ResizeObserver loop completed with undelivered notifications')) {
      return
    }
    originalConsoleError.apply(this, args)
  }
}

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  // 过滤ResizeObserver和路由相关的错误
  if (err && err.message && 
      (err.message.includes('ResizeObserver loop completed with undelivered notifications') ||
       err.message.includes('Navigation cancelled') ||
       err.message.includes('Navigation duplicated'))) {
    return
  }
  console.error('Vue应用错误:', err, info)
}

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)

// 路由错误处理
router.onError((error) => {
  if (error.message && 
      (error.message.includes('Navigation cancelled') ||
       error.message.includes('Navigation duplicated') ||
       error.message.includes('ResizeObserver'))) {
    return
  }
  console.warn('路由错误:', error)
})

app.mount('#app')
