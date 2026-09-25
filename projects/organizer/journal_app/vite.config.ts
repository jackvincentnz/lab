import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig } from "vitest/config";
import { bazelCoverage } from "../../../tools/bazel/vitest/coverage.ts";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  /*
    The vite devserver needs to make requests to "base: /journal/" when behind the
    proxy since the $http_referer can't be used to identify the right app to proxy to.
    Keep aligned with:
    - infra/local/proxy/nginx.conf: "proxy_pass"
    - //projects/organizer/journal_app:tar: "package_dir"
  */
  base: "/journal/",
  plugins: [
    preserveHtmlEntrySymlinks(),
    watchNodeModules(["@lab/bubbles"]),
    react(),
  ],
  resolve: {
    // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
    // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
    preserveSymlinks: process.env.VITEST === "true",
  },
  test: {
    // Coverage runfiles also contain TS sources for remapping; run each test once.
    include: ["src/**/*.{test,spec}.js"],
    coverage: bazelCoverage(["src/**/*.js"]),
    environment: "jsdom",
    setupFiles: "./src/test/setup.js",
    css: false,
    // Journal has no component tests yet; keep the standard targets usable.
    passWithNoTests: true,
  },
  server: {
    port: 3004,
    proxy: {
      "/graphql": "http://localhost:4000",
    },
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

// https://github.com/vitejs/vite/issues/8619

export function watchNodeModules(modules: string[]): PluginOption {
  return {
    name: "watch-node-modules",
    config() {
      return {
        server: {
          watch: {
            ignored: modules.map((m) => `!**/node_modules/${m}/**`),
          },
        },
        optimizeDeps: {
          exclude: modules,
          include: ["@mantine/core", "@lab/bubbles", "@apollo/client"],
        },
      };
    },
  };
}
