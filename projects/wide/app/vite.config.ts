import { isAbsolute, resolve } from "node:path";
import { searchForWorkspaceRoot, type PluginOption } from "vite";
import { defineConfig, mergeConfig } from "vitest/config";
import { bazelVitestConfig } from "../../../tools/bazel/vitest/config.ts";

const servicePort = Number(process.env.WIDE_PORT ?? 8095);

// https://vitejs.dev/config/
export default mergeConfig(
  bazelVitestConfig({
    // SWC emits an empty, unmappable JS file for this type-only module.
    coverageExclude: ["src/lib/links.js"],
    // The shared setup already covers this app.
    setupFiles: [],
  }),
  defineConfig({
    plugins: [preserveHtmlEntrySymlinks()],
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
          target: `http://127.0.0.1:${servicePort}`,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
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
