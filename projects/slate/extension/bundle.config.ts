import { defineConfig } from "vite";

// The extension ships no node_modules.
export default defineConfig({
  build: {
    ssr: "src/main.js",
    outDir: "bundle",
    rolldownOptions: {
      output: { entryFileNames: "server.mjs" },
    },
  },
  ssr: {
    noExternal: true,
  },
});
