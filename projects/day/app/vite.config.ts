import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "@lab/vitest-config";
import react from "@vitejs/plugin-react";

const servicePort = Number(process.env.DAY_PORT || 5297);

// https://vitejs.dev/config/
export default mergeConfig(
  bazelVitestConfig({ setupFiles: [] }),
  defineConfig({
    plugins: [preserveHtmlEntrySymlinks(), react()],
    server: {
      port: Number(process.env.DAY_APP_PORT ?? 5196),
      proxy: {
        "/api/": `http://127.0.0.1:${servicePort}`,
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
