import react from "@vitejs/plugin-react";
import { defineConfig } from "electron-vite";

// The repository ignores dist for git, ESLint and Prettier.
export default defineConfig({
  main: {
    build: { outDir: "dist/main" },
  },
  renderer: {
    build: { outDir: "dist/renderer" },
    plugins: [react()],
  },
});
