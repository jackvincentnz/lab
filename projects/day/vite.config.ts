import { defineConfig } from "vite";

// Bundles Electron main and the service with their npm dependencies, so the app needs no
// node_modules. Each entry keeps its path, so main finds the service where it does in Bazel.
export default defineConfig({
  build: {
    ssr: true,
    outDir: "bundle",
    rolldownOptions: {
      input: {
        "desktop/src/main": "desktop/src/main.js",
        "service/src/main": "service/src/main.js",
      },
      output: { entryFileNames: "[name].js" },
    },
  },
  ssr: {
    external: ["electron"],
    noExternal: true,
  },
});
