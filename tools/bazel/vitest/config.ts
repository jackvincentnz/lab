import type { UserConfig } from "vitest/config";
import { bazelCoverage } from "./coverage.ts";

interface Options {
  include?: string[];
  coverageInclude?: string[];
  coverageExclude?: string[];
  /** App-specific setup files, run after `@lab/test-utils/setup`. */
  setupFiles?: string | string[];
}

/** Shared Vitest defaults; the default paths fit an `fe_app`, relative to its Vite root. */
export function bazelVitestConfig({
  include = ["src/**/*.{test,spec}.js"],
  coverageInclude = ["src/**/*.js"],
  coverageExclude = [],
  setupFiles = "./src/test/setup.js",
}: Options = {}): UserConfig {
  return {
    // Vite cannot find the workspace root in runfiles, so let it read shared setup outside the app.
    server: process.env.TEST_SRCDIR
      ? {
          fs: {
            allow: [`${process.env.TEST_SRCDIR}/${process.env.TEST_WORKSPACE}`],
          },
        }
      : {},
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
      // A setup file runs before every test file, so shared cleanup hooks cover each file.
      setupFiles: ["@lab/test-utils/setup", setupFiles].flat(),
      // Per-file module loading and jsdom setup took most of the test time.
      isolate: false,
      // Parsing CSS is slow, and no test relies on it.
      css: false,
    },
  };
}
