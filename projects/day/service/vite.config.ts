import { defineConfig } from "vitest/config";
import { bazelVitestConfig } from "../../../tools/bazel/vitest/config.ts";

export default defineConfig(
  bazelVitestConfig({ environment: "node", setupFiles: [] }),
);
