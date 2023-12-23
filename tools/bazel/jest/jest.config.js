const path = require("path");

module.exports = {
  testEnvironment: "jsdom",
  testMatch: ["**/*test.js"],
  transform: {
    "^.+\\.css$": "jest-transform-stub",
  },
  setupFilesAfterEnv: [path.join(process.cwd(), "tools/bazel/jest/setup.js")],
};
