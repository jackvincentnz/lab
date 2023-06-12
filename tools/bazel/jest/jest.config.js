module.exports = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.css$": "jest-transform-stub",
    "screenfull/index.js$": "@swc/jest",
  },
  transformIgnorePatterns: ["/node_modules/(?!(screenfull/index.js))/"],
};
