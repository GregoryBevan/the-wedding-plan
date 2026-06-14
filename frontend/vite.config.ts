import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      input: {
        public: resolve(__dirname, 'public/index.html'),
        backoffice: resolve(__dirname, 'backoffice/index.html'),
      },
    },
  },
})
