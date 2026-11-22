import { defineConfig } from "vitest/config";
import { bazelVitestConfig } from "@lab/vitest-config";

export default defineConfig(
  bazelVitestConfig({ environment: "node", setupFiles: [] }),
);
