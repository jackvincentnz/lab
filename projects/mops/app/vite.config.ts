import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig } from "vitest/config";
import { bazelCoverage } from "../../../tools/bazel/vitest/coverage.ts";

// https://vitejs.dev/config/
export default defineConfig(({ command }) => {
  // The gateway serves the app under /mops in local dev. The nginx image keeps serving at /
  // until the public URL structure is decided (https://github.com/jackvincentnz/lab/issues/960).
  const base = command === "serve" ? "/mops/" : "/";
  const api = `${base}api`;
  const ws = `${base}ws`;

  return {
    base,
    plugins: [preserveHtmlEntrySymlinks()],
    resolve: {
      // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
      // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
      preserveSymlinks: process.env.VITEST === "true",
    },
    server: {
      proxy: {
        [api]: {
          target: "http://127.0.0.1:8080",
          changeOrigin: true,
          rewrite: (path) => path.slice(api.length),
        },
        [ws]: {
          target: "ws://127.0.0.1:8080",
          changeOrigin: true,
          rewrite: (path) => path.slice(ws.length),
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
  };
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
