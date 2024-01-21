const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    specPattern: ["*.cy.js"],
    supportFile: false,
    setupNodeEvents(on) {
      on("before:browser:launch", (browser, launchOptions) => {
        launchOptions.args.push("--disable-gpu-shader-disk-cache");
      });
    },
  },
  video: false, // TODO: remove in v13
});
