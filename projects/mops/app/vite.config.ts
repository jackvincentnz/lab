import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig } from "vitest/config";
import { bazelCoverage } from "../../../tools/bazel/vitest/coverage.ts";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [preserveHtmlEntrySymlinks()],
  resolve: {
    // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
    // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
    preserveSymlinks: process.env.VITEST === "true",
  },
  server: {
    proxy: {
      "/api": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
      "/ws": {
        target: "ws://127.0.0.1:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws/, ""),
      },
    },
  },
  test: {
    // Coverage runfiles also contain TS sources for remapping; run each test once.
    include: ["src/**/*.{test,spec}.js"],
    coverage: bazelCoverage(
      ["src/**/*.js"],
      // This module contains only types, so SWC emits an empty, unmappable JS file.
      ["src/pages/spend/components/spend-table/types.js"],
    ),
    environment: "jsdom",
    setupFiles: "./src/test/setup.js",
    // parsing CSS is slow, if you don't have tests that rely on CSS disable it
    css: false,
  },
});

function preserveHtmlEntrySymlinks(): PluginOption {
  return {
    name: "preserve-html-entry-symlinks",
    apply: "build",
    enforce: "pre",
    resolveId(id) {
      // Vite 8 realpaths Bazel's HTML input outside config.root unless this entry stays symlinked.
      // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
      if (isAbsolute(id) && id.endsWith(".html")) {
        return id;
      }
    },
  };
}
