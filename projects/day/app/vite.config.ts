import { isAbsolute } from "node:path";
import type { PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "@lab/vitest-config";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default mergeConfig(
  bazelVitestConfig({ setupFiles: [] }),
  defineConfig({
    // The desktop app loads the build from a file, where absolute asset paths do not resolve.
    base: "./",
    plugins: [preserveHtmlEntrySymlinks(), react()],
    server: {
      port: Number(process.env.DAY_APP_PORT ?? 5196),
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
