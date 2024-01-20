import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: "apps/**/e2e/**/*.cy.ts",
    supportFile: false,
    baseUrl: "http://localhost:8080",
    video: false, // TODO: remove in v13
  },
});
