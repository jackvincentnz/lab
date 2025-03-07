import { defineConfig } from "vitest/config";

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    proxy: {
      "/graphql": "http://127.0.0.1:8080",
      "/chat": "http://127.0.0.1:3000",
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.js",
    // parsing CSS is slow, if you don't have tests that rely on CSS disable it
    css: false,
  },
});
