module.exports = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.css$": "jest-transform-stub",
    "\\.js$": ["@swc/jest"],
  },
  transformIgnorePatterns: ["/node_modules/(?!(screenfull/index.js))/"],
};
