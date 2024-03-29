import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    setupNodeEvents(on) {
      on("before:browser:launch", (_, launchOptions) => {
        launchOptions.args.push("--disable-gpu-shader-disk-cache");
      });
    },
    specPattern: ["*.cy.js"],
    supportFile: false,
    video: false, // TODO: remove in v13,
  },
});
