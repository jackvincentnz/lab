import { writeFileSync } from "node:fs";
import type { UserConfig } from "vitest/config";
import { bazelCoverage } from "./coverage.ts";

interface Options {
  include?: string[];
  coverageInclude?: string[];
  coverageExclude?: string[];
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
      // Without isolation Vitest restores only `vi.spyOn` spies between files.
      restoreMocks: true,
      unstubGlobals: true,
      unstubEnvs: true,
      // Per-file module loading and jsdom setup took most of the test time.
      isolate: false,
      // Parsing CSS is slow, and no test relies on it.
      css: false,
      shard: bazelShard(),
      sequence: bazelSequence(),
    },
  };
}

/** Map Bazel test sharding onto Vitest's `<index>/<count>` file slices. */
function bazelShard(): string | undefined {
  const { TEST_TOTAL_SHARDS, TEST_SHARD_INDEX, TEST_SHARD_STATUS_FILE } =
    process.env;
  if (!TEST_TOTAL_SHARDS) {
    return undefined;
  }
  // Bazel fails a sharded test that does not acknowledge sharding.
  if (TEST_SHARD_STATUS_FILE) {
    writeFileSync(TEST_SHARD_STATUS_FILE, "");
  }
  return `${Number(TEST_SHARD_INDEX) + 1}/${TEST_TOTAL_SHARDS}`;
}

interface Sequence {
  shuffle: { files: boolean };
  seed: number;
}

/**
 * Shuffle test files only when Bazel supplies a seed, so a normal run keeps its fixed order.
 * Without isolation, a test can pass only because an earlier file left state behind.
 */
function bazelSequence(): Sequence | undefined {
  const { TEST_RANDOM_SEED } = process.env;
  if (!TEST_RANDOM_SEED) {
    return undefined;
  }
  return { shuffle: { files: true }, seed: Number(TEST_RANDOM_SEED) };
}
