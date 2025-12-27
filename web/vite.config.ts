import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 8088,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        // 不再重写路径，保留/api前缀与后端匹配
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
}) 