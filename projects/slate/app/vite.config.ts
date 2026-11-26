import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "@lab/vitest-config";
import react from "@vitejs/plugin-react";

const servicePort = Number(process.env.SLATE_PORT || 5297);

export default mergeConfig(
  bazelVitestConfig({ setupFiles: [] }),
  defineConfig({
    plugins: [preserveHtmlEntrySymlinks(), react()],
    server: {
      port: Number(process.env.SLATE_APP_PORT ?? 5196),
      proxy: {
        // The string shorthand would rewrite Host to the service's, and the service refuses a write
        // whose Origin does not match its Host.
        "/api/": {
          target: `http://127.0.0.1:${servicePort}`,
          changeOrigin: false,
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
      if (isAbsolute(id) && id.endsWith(".html")) {
        return id;
      }
    },
  };
}
