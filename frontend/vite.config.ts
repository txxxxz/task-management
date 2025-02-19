import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        quietDeps: true,
        additionalData: `
          @use "sass:color";
          @function darken($color, $amount) {
            @return color.adjust($color, $lightness: -$amount);
          }
        `,
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
})
