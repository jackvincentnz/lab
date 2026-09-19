import { defineConfig } from "@playwright/test";

export default defineConfig({
  globalSetup: "./runner.ts",
});
