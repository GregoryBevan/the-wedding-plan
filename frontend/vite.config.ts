import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  test: {
    environment: 'jsdom',
    include: ['backoffice/src/**/*.spec.ts'],
    globals: true,
  },
  build: {
    rollupOptions: {
      input: {
        public: resolve(__dirname, 'public/index.html'),
        backoffice: resolve(__dirname, 'backoffice/index.html'),
      },
    },
  },
})
