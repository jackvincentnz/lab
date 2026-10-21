import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "../../../tools/bazel/vitest/config.ts";

const servicePort = Number(process.env.MOPS_PORT ?? 8080);

// https://vitejs.dev/config/
export default mergeConfig(
  bazelVitestConfig({
    // SWC emits an empty, unmappable JS file for this type-only module.
    coverageExclude: ["src/pages/spend/components/spend-table/types.js"],
  }),
  defineConfig({
    test: { maxWorkers: 2 },
    plugins: [preserveHtmlEntrySymlinks()],
    server: {
      // Node binds "localhost" to ::1 only on macOS, and the gateway connects to the app over IPv4.
      host: "127.0.0.1",
      port: Number(process.env.MOPS_APP_PORT ?? 5173),
      proxy: {
        "/api": {
          target: `http://127.0.0.1:${servicePort}`,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
        "/ws": {
          target: `ws://127.0.0.1:${servicePort}`,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/ws/, ""),
        },
      },
    },
  }),
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
