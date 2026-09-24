import { basename, dirname, join } from "node:path";
import type { CoverageV8Options } from "vitest/node";

/** Feed native Vitest coverage into Bazel's existing LCOV merge and cache. */
export function bazelCoverage(
  include: string[],
  exclude: string[] = [],
): CoverageV8Options {
  const output = process.env.COVERAGE_OUTPUT_FILE;
  const projectRoot = process.env.TEST_SRCDIR
    ? join(process.env.TEST_SRCDIR, process.env.TEST_WORKSPACE!)
    : process.cwd();

  return {
    provider: "v8",
    enabled: Boolean(output),
    // Runfiles contain SWC output. Source maps restore repository TS/TSX paths.
    // Explicit includes also count production files that no test imports.
    include,
    exclude: [
      "**/node_modules/**",
      "**/*.test.*",
      "**/*.spec.*",
      "**/*.stories.*",
      "**/*.d.ts",
      "**/__tests__/**",
      "**/__fixtures__/**",
      "**/__generated__/**",
      "**/test/**",
      ...exclude,
    ],
    reportsDirectory: output ? dirname(output) : "coverage",
    // The Bazel output directory also contains test results; never clean it.
    clean: !output,
    reporter: [
      [
        "lcovonly",
        { file: output ? basename(output) : "lcov.info", projectRoot },
      ],
    ],
  };
}
