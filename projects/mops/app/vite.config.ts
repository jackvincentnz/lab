import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "../../../tools/bazel/vitest/config.ts";

// https://vitejs.dev/config/
export default defineConfig(
  mergeConfig(
    bazelVitestConfig({
      include: ["src/**/*.{test,spec}.js"],
      coverageInclude: ["src/**/*.js"],
      // SWC emits an empty, unmappable JS file for this type-only module.
      coverageExclude: ["src/pages/spend/components/spend-table/types.js"],
      setupFiles: "./src/test/setup.js",
    }),
    {
      plugins: [preserveHtmlEntrySymlinks()],

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
    },
  ),
);

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
