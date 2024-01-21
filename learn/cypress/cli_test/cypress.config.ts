import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: ["cli_test.cy.ts"],
    supportFile: false,
    setupNodeEvents(on) {
      on("before:browser:launch", (browser, launchOptions) => {
        launchOptions.args.push("--disable-gpu-shader-disk-cache");
      });
    },
  },
  video: false, // TODO: remove in v13
});
