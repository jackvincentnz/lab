import { defineConfig } from "@playwright/test";

export default defineConfig({
  globalSetup: "./global-setup.ts",
  outputDir: process.env["TEST_UNDECLARED_OUTPUTS_DIR"],
});
