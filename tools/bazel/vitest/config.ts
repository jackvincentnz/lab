import type { UserConfig } from "vitest/config";
import { bazelCoverage } from "./coverage.ts";

interface Options {
  include: string[];
  coverageInclude: string[];
  coverageExclude?: string[];
  setupFiles?: string | string[];
}

/** Paths are relative to the caller's Vite root, for both Bazel layouts. */
export function bazelVitestConfig({
  include,
  coverageInclude,
  coverageExclude = [],
  setupFiles = "@lab/test-utils/setup",
}: Options): UserConfig {
  return {
    resolve: {
      // Keep Vitest setup and test files inside Bazel's sandboxed runfiles tree.
      // TODO: Remove when https://github.com/jackvincentnz/lab/issues/771 is resolved.
      preserveSymlinks: process.env.VITEST === "true",
    },
    test: {
      // Coverage runfiles also contain TS sources for remapping; run each test once.
      include,
      coverage: bazelCoverage(coverageInclude, coverageExclude),
      environment: "jsdom",
      setupFiles,
      // These suites do not rely on CSS parsing.
      css: false,
      server: {
        deps: {
          // Workspace packages contain compiled JS with Vite-style imports.
          // Transform them just as Vite does for the app, instead of using Node ESM.
          inline: ["@lab/bubbles"],
        },
      },
    },
  };
}
