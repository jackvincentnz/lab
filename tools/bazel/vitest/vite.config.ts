import { defineConfig } from "vitest/config";
import { bazelVitestConfig } from "./config.ts";

// Library tests run from the workspace root.
export default defineConfig(
  bazelVitestConfig({
    include: ["**/*.{test,spec}.js"],
    coverageInclude: ["projects/**/src/**/*.js", "libs/**/*.js"],
    setupFiles: "@lab/test-utils/setup",
  }),
);
