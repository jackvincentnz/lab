import { defineConfig } from "vitest/config";
import { bazelCoverage } from "./coverage.ts";

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
    // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
    preserveSymlinks: true,
  },
  test: {
    // Coverage runfiles also contain TS sources for remapping; run each test once.
    include: ["**/*.{test,spec}.js"],
    coverage: bazelCoverage(["projects/**/src/**/*.js", "libs/**/*.js"]),
    environment: "jsdom",
    setupFiles: "./tools/bazel/vitest/setup.js",
    // parsing CSS is slow, if you don't have tests that rely on CSS disable it
    css: false,
  },
});
