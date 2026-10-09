import { defineConfig } from "vitest/config";
import { bazelVitestConfig } from "./config.ts";

export default defineConfig(
  bazelVitestConfig({
    include: ["**/*.{test,spec}.js"],
    coverageInclude: ["projects/**/src/**/*.js", "libs/**/*.js"],
  }),
);
