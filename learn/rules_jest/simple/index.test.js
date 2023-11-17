const { it, expect } = require("@jest/globals");

const index = require(".");

it("should work", () => {
  expect(index).toBe("test");
});
