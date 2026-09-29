import { isAbsolute, resolve } from "node:path";
import { searchForWorkspaceRoot, type PluginOption } from "vite";
import { defineConfig } from "vitest/config";
import { bazelCoverage } from "../../../tools/bazel/vitest/coverage.ts";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [preserveHtmlEntrySymlinks()],
  resolve: {
    // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
    preserveSymlinks: process.env.VITEST === "true",
  },
  server: {
    port: 5195,
    strictPort: true,
    fs: {
      // The guides drawer renders the shared refinement guides from the skills directory.
      allow: [
        searchForWorkspaceRoot(process.cwd()),
        resolve(process.cwd(), "../skills/wide-refine/references"),
      ],
    },
    proxy: {
      "/api/graphql": {
        target: `http://127.0.0.1:${process.env.WIDE_PORT || "8095"}`,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
  test: {
    // Coverage runfiles also contain TS sources for remapping; run each test once.
    include: ["src/**/*.{test,spec}.js"],
    coverage: bazelCoverage(
      ["src/**/*.js"],
      // This module contains only types, so SWC emits an empty, unmappable JS file.
      ["src/lib/links.js"],
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
      if (isAbsolute(id) && id.endsWith(".html")) {
        return id;
      }
    },
  };
}
