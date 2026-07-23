import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

const appVersion = process.env.npm_package_version ?? '0.0.0'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  define: {
    __APP_VERSION__: JSON.stringify(appVersion),
  },
  test: {
    environment: 'jsdom',
    include: ['backoffice/src/**/*.spec.ts', 'public/src/**/*.spec.ts'],
    globals: true,
    setupFiles: ['./vitest.setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text-summary', 'json-summary', 'cobertura'],
    },
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
