import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const backendOrigin = process.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: backendOrigin,
        changeOrigin: true,
      },
      '/auth': {
        target: backendOrigin,
        changeOrigin: true,
      },
      '/images': {
        target: backendOrigin,
        changeOrigin: true,
      },
    },
  },
})
