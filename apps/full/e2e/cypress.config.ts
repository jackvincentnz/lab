import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: ["*.cy.ts"],
    supportFile: false,
    baseUrl: "http://localhost:8080",
    video: false, // TODO: remove in v13,
    setupNodeEvents(on) {
      on("before:browser:launch", (_browser, launchOptions) => {
        launchOptions.args.push("--disable-gpu-shader-disk-cache");
      });
    },
  },
});
