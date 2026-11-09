import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.js",
    clearMocks: true,
  },
  server: {
    host: true,
    allowedHosts: [".devtunnels.ms", ".app.github.dev"],
    proxy: {
      "/api/v1": {
        target: "http://localhost:8080",
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on("proxyReq", (proxyRequest) => {
            // Para el backend sigue siendo una petición del mismo sitio. Se conserva
            // Sec-Fetch-Site, que permite rechazar formularios iniciados desde otro sitio.
            proxyRequest.removeHeader("origin");
          });
        },
      },
    },
  },
})
