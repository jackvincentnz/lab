import { defineConfig } from "vitest/config";

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
    // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
    preserveSymlinks: true,
  },
  test: {
    environment: "jsdom",
    setupFiles: "./tools/bazel/vitest/setup.js",
    // parsing CSS is slow, if you don't have tests that rely on CSS disable it
    css: false,
  },
});
