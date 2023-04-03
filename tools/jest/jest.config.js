const path = require("path");

module.exports = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.svg$": "jest-transform-stub",
    "^.+\\.css$": "jest-transform-stub",
  },
  setupFilesAfterEnv: ["./tools/jest/setupTests.js"],
  rootDir: path.resolve("."),
};
