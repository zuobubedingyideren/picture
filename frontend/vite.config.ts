import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: {
      // 代理后端API请求
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true
      },
      // 代理腾讯云COS图片请求，解决ERR_BLOCKED_BY_ORB错误
      '/cos-proxy': {
        target: 'https://picture-1356335042.cos.ap-chongqing.myqcloud.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/cos-proxy/, ''),
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 添加必要的请求头以避免403错误
            proxyReq.setHeader('Referer', 'https://picture-1356335042.cos.ap-chongqing.myqcloud.com/');
            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36');
          });
        }
      }
    }
  },
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
