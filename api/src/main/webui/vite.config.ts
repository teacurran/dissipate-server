import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig({
  base: '',
  plugins: [
    vue(),
    vueJsx(),
  ],
  server: {
    // this ensures that the browser opens upon server start
    open: false,
    // this sets a default port to 3000, you can change this
    port: 3000,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
