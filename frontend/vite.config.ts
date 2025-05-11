import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        quietDeps: true,
        additionalData: `@use "sass:color";`,
        sassOptions: {
          quietDeps: true,
          logger: {
            warn: () => {},
            debug: () => {}
          }
        }
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('vue')) return 'vendor_vue'
            if (id.includes('element-plus')) return 'vendor_element'
            return 'vendor_other'
          }
          if (id.includes('src/views') && !id.includes('components')) {
            return 'views_' + id.split('/').reverse()[0].split('.')[0]
          }
        }
      }
    }
  },
  optimizeDeps: {
    exclude: [
      'js-cookie',
      'path',
      'axios'
    ]
  }
})
